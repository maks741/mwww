package org.maks.mwww_daemon.backend.spotify;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpServer;
import org.maks.mwww_daemon.backend.cmd.CmdService;
import org.maks.mwww_daemon.shared.domain.dto.SpotifyAuthResponseDTO;
import org.maks.mwww_daemon.shared.utils.Config;
import org.maks.mwww_daemon.shared.utils.ResourceUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Contains Spotify Web API auth logic
 * Uses the PKCE Authorization Flow
 * See
 * <a href="https://developer.spotify.com/documentation/web-api/concepts/authorization">Spotify Web API Authorization</a>
 * <a href="https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow">PKCE Authorization Flow</a>
 * <a href="https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens">Refresh Access Token</a>
 */
public class SpotifyPKCEAuth {

    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final String clientId = Config.spotifyClientId();
    private final String redirectUri = Config.spotifyRedirectUri();

    public String accessToken() {
        SpotifyAuthResponseDTO authData;

        try {
            String cachedRefreshToken = Files.readString(ResourceUtils.credentialsPath());
            authData = refreshToken(cachedRefreshToken);
        } catch (NoSuchFileException e) {
            authData = authorize();
        } catch (IOException | JsonSyntaxException e) {
            throw new RuntimeException(e);
        }

        writeRefreshToken(authData.refreshToken());

        return authData.accessToken();
    }

    private SpotifyAuthResponseDTO authorize() {
        String codeVerifier = generateCodeVerifier();
        String authScope = "playlist-modify-public";
        String codeChallenge = generateCodeChallenge(codeVerifier);

        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        startCallbackServer(codeFuture);

        URI authUri = buildAuthUri(codeChallenge, authScope);

        // open url in default browser
        CmdService cmdService = new CmdService();
        cmdService.runCmdCommand("xdg-open", authUri.toString());

        // wait until Spotify redirects back with ?code=
        String code;
        try {
            code = codeFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        String authResponseStr = requestAccessToken(code, codeVerifier);
        return new Gson().fromJson(authResponseStr, SpotifyAuthResponseDTO.class);
    }

    private SpotifyAuthResponseDTO refreshToken(String refreshToken) {
        String body = new StringJoiner("&")
                .add("grant_type=refresh_token")
                .add("refresh_token=" + urlEncode(refreshToken))
                .add("client_id=" + urlEncode(clientId))
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBodyStr = response.body();
            return new Gson().fromJson(responseBodyStr, SpotifyAuthResponseDTO.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private URI buildAuthUri(String codeChallenge, String authScope) {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("scope", authScope);
        params.put("redirect_uri", redirectUri);
        params.put("code_challenge_method", "S256");
        params.put("code_challenge", codeChallenge);

        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        return URI.create(AUTH_URL + "?" + query);
    }

    private void startCallbackServer(CompletableFuture<String> future) {
        URI uri = URI.create(redirectUri);
        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        String targetUrlParam = "code";

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.createContext(uri.getPath(), exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> urlParamsMap = parseQuery(query);

            String code;
            if (!urlParamsMap.containsKey(targetUrlParam)) {
                server.stop(0);
                throw new RuntimeException("Could not find value of 'code' url param");
            }
            code = urlParamsMap.get(targetUrlParam);

            String response = "Authorization successful. You can close this window.";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            future.complete(code);
            server.stop(0);
        });

        server.start();
    }

    private String requestAccessToken(String code, String codeVerifier) {
        String body = new StringJoiner("&")
                .add("client_id=" + urlEncode(clientId))
                .add("grant_type=authorization_code")
                .add("code=" + urlEncode(code))
                .add("redirect_uri=" + urlEncode(redirectUri))
                .add("code_verifier=" + urlEncode(codeVerifier))
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallenge(String verifier) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hash = sha256.digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            map.put(parts[0], parts.length > 1 ? parts[1] : "");
        }
        return map;
    }

    private void writeRefreshToken(String refreshToken) {
        try {
            StandardOpenOption[] writeOptions = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE };
            Files.writeString(ResourceUtils.credentialsPath(), refreshToken, writeOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

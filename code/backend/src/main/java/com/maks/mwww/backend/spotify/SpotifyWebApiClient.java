package com.maks.mwww.backend.spotify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maks.mwww.domain.dto.AddTracksToPlaylistRequestDTO;
import com.maks.mwww.domain.dto.DeleteTrackFromPlaylistRequestDTO;
import com.maks.mwww.domain.exception.SpotifyWebApiException;
import com.maks.mwww.backend.utils.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class SpotifyWebApiClient {

    private static final Logger LOG = Logger.getLogger(SpotifyWebApiClient.class.getName());

    private static final SpotifyPKCEAuth spotifyPKCEAuth = new SpotifyPKCEAuth();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String API_URL = "https://api.spotify.com/v1";
    private static final String PLAYLISTS_TRACKS_API_URL = API_URL + "/playlists/" + Config.spotifyPlaylistId() + "/tracks";
    private static final String SEARCH_API_URL = API_URL + "/search?q=%s&type=%s&limit=10";

    public void addTrackToPlaylist(String trackUri) {
        var requestDto = new AddTracksToPlaylistRequestDTO(List.of(trackUri), 0);
        HttpRequest request = buildRequest(PLAYLISTS_TRACKS_API_URL, "POST", requestDto);

        executeRequest(PLAYLISTS_TRACKS_API_URL, request, 201);
    }

    public void deleteTrackFromPlaylist(String trackUri) {
        var requestDto = new DeleteTrackFromPlaylistRequestDTO(
                List.of(new DeleteTrackFromPlaylistRequestDTO.DeleteTrackFromPlaylistItemRequestDto(trackUri))
        );
        HttpRequest request = buildRequest(PLAYLISTS_TRACKS_API_URL, "DELETE", requestDto);

        executeRequest(PLAYLISTS_TRACKS_API_URL, request, 200);
    }

    public String search(String query) {
        String types = "album,artist,playlist,track,show,episode,audiobook";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format(SEARCH_API_URL, encodedQuery, types);

        HttpRequest request = buildGetRequest(url);
        String responseBody = executeRequest(url, request, 200);

        JsonNode root;
        try {
            root = mapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String[] responseCategories = {"tracks", "playlists", "artists", "albums", "shows", "episodes", "audiobooks"};
        String uriField = "uri";
        for (String categoryKey : responseCategories) {
            if (!root.has(categoryKey)) {
                continue;
            }

            JsonNode categoryObj = root.get(categoryKey);
            if (!categoryObj.has("items") || !categoryObj.get("items").isArray()) {
                continue;
            }
            JsonNode items = categoryObj.get("items");

            if (items == null || items.isEmpty()) {
                continue;
            }

            // return the 'uri' of the very first item in the first available category
            JsonNode mostRelevant = items.get(0);
            if (!mostRelevant.has(uriField)) {
                LOG.warning("Search result missing URI: " + mostRelevant);
                continue;
            }
            return mostRelevant.get(uriField).asText();
        }

        return null;
    }

    private String executeRequest(String uri, HttpRequest request, int expectedResponseCode) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseStatusCode = response.statusCode();
            String responseBody = response.body();

            LOG.info(request.method() + " request to: " + uri + " returned " + responseStatusCode + " status code");

            if (responseStatusCode != expectedResponseCode) {
                throw new SpotifyWebApiException(
                        "Request to " + uri + " did not finish successfully.\nResponse body:\n" + responseBody,
                        responseBody
                );
            }

            return responseBody;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildRequest(String uri, String method, Object body) {
        String bodyStr;
        try {
            bodyStr = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return addHeaders(HttpRequest.newBuilder().uri(URI.create(uri)))
                .method(method, HttpRequest.BodyPublishers.ofString(bodyStr))
                .build();
    }

    private HttpRequest buildGetRequest(String uri) {
        return addHeaders(HttpRequest.newBuilder().uri(URI.create(uri)))
                .GET()
                .build();
    }

    private HttpRequest.Builder addHeaders(HttpRequest.Builder builder) {
        String accessToken = spotifyPKCEAuth.accessToken();

        return builder
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }
}

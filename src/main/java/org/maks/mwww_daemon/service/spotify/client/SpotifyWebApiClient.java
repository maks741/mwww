package org.maks.mwww_daemon.service.spotify.client;

import com.google.gson.Gson;
import org.maks.mwww_daemon.exception.SpotifyWebApiException;
import org.maks.mwww_daemon.service.spotify.auth.SpotifyPKCEAuth;
import org.maks.mwww_daemon.utils.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Logger;

public class SpotifyWebApiClient {

    private static final Logger LOG = Logger.getLogger(SpotifyWebApiClient.class.getName());

    private static final SpotifyPKCEAuth spotifyPKCEAuth = new SpotifyPKCEAuth();

    private static final String API_URL = "https://api.spotify.com/v1";
    private static final String PLAYLIST_ID = Config.spotifyPlaylistId();
    private static final String PLAYLISTS_TRACKS_API_URL = API_URL + "/playlists/" + PLAYLIST_ID + "/tracks";

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

    private void executeRequest(String uri, HttpRequest request, int expectedResponseCode) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseStatusCode = response.statusCode();

            LOG.info("POST request to: " + uri + " returned " + responseStatusCode + " status code");

            if (responseStatusCode != expectedResponseCode) {
                String responseBody = response.body();
                throw new SpotifyWebApiException(
                        "Request to " + uri + " did not finish successfully.\nResponse body:\n" + responseBody,
                        responseBody
                );
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildRequest(String uri, String method, Object body) {
        String accessToken = spotifyPKCEAuth.accessToken();
        String bodyStr = new Gson().toJson(body);

        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(bodyStr))
                .build();
    }
}

package org.maks.mwww_daemon.shared.domain.dto;

import com.google.gson.annotations.SerializedName;

public record SpotifyAuthResponseDTO(
        @SerializedName("access_token")
        String accessToken,
        @SerializedName("token_type")
        String tokenType,
        @SerializedName("expires_in")
        int expiresIn,
        @SerializedName("refresh_token")
        String refreshToken,
        @SerializedName("scope")
        String scope
) {
}

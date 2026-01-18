package com.maks.mwww.domain.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SpotifyAuthResponseDTO(
        String accessToken,
        String tokenType,
        int expiresIn,
        String refreshToken,
        String scope
) {
}

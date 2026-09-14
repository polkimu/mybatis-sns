package net.likelion.bebc25.sns.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
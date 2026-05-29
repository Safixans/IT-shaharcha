package com.itshaharcha.auth.dto.response;

/** Token pair returned on successful login/refresh. */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        AccountResponse account) {

    public static AuthResponse bearer(String accessToken, String refreshToken,
                                      long expiresInSeconds, AccountResponse account) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, account);
    }
}

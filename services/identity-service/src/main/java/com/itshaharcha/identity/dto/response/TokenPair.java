package com.itshaharcha.identity.dto.response;

/** Token pair returned on successful login/refresh (spec TokenPair). */
public record TokenPair(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        AccountResponse account) {

    public static TokenPair bearer(String accessToken, String refreshToken,
                                   long expiresIn, AccountResponse account) {
        return new TokenPair(accessToken, refreshToken, "Bearer", expiresIn, account);
    }
}

package com.itshaharcha.analytics.security;

/** Token kinds minted by identity-service; analytics only accepts ACCESS. */
public enum TokenType {
    ACCESS,
    REFRESH
}

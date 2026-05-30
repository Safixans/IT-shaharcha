package com.itshaharcha.assessment.security;

/** Token kinds minted by identity-service; assessment only accepts ACCESS. */
public enum TokenType {
    ACCESS,
    REFRESH
}

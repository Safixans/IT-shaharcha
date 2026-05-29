package com.itshaharcha.common.security;

/** Header names the gateway injects after validating a JWT, trusted downstream. */
public final class HeaderNames {

    public static final String ACCOUNT_ID = "X-Account-Id";
    public static final String USERNAME = "X-Username";
    public static final String ROLES = "X-Roles";

    private HeaderNames() {
    }
}

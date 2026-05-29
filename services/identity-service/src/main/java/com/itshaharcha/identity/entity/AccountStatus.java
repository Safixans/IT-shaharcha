package com.itshaharcha.identity.entity;

public enum AccountStatus {
    /** Registered but email/OTP not yet verified. */
    PENDING,
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}

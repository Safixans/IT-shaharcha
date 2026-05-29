package com.itshaharcha.auth.entity;

public enum AccountStatus {
    /** Registered but email/OTP not yet verified. */
    PENDING,
    ACTIVE,
    SUSPENDED,
    DISABLED
}

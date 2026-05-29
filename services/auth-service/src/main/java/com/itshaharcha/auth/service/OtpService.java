package com.itshaharcha.auth.service;

import com.itshaharcha.auth.entity.Account;
import com.itshaharcha.auth.entity.OtpPurpose;

public interface OtpService {

    /** Issues a fresh OTP for the account/purpose and dispatches it (email/SMS). */
    void issue(Account account, OtpPurpose purpose);

    /** Verifies and consumes the latest unused OTP; throws if invalid/expired. */
    void verifyAndConsume(Account account, OtpPurpose purpose, String rawCode);
}

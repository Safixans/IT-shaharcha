package com.itshaharcha.identity.service.impl;

import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.OtpCode;
import com.itshaharcha.identity.entity.OtpPurpose;
import com.itshaharcha.identity.repository.OtpCodeRepository;
import com.itshaharcha.identity.service.OtpService;
import com.itshaharcha.identity.util.HashUtil;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(10);

    private final OtpCodeRepository otpCodeRepository;

    @Override
    @Transactional
    public void issue(Account account, OtpPurpose purpose) {
        String rawCode = HashUtil.generateOtp();

        OtpCode otp = new OtpCode();
        otp.setAccount(account);
        otp.setPurpose(purpose);
        otp.setCodeHash(HashUtil.sha256(rawCode));
        otp.setExpiresAt(Instant.now().plus(OTP_TTL));
        otpCodeRepository.save(otp);

        // TODO: dispatch via notification-service (email/SMS). Logged for now.
        log.info("Issued {} OTP for account {}: {}", purpose, account.getId(), rawCode);
    }

    @Override
    @Transactional
    public void verifyAndConsume(Account account, OtpPurpose purpose, String rawCode) {
        OtpCode otp = otpCodeRepository
                .findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(account, purpose)
                .orElseThrow(() -> ApplicationException.badRequest("No active code to verify"));

        if (!otp.isUsable() || !otp.getCodeHash().equals(HashUtil.sha256(rawCode))) {
            throw ApplicationException.badRequest("Invalid or expired code");
        }
        otp.setConsumed(true);
        otpCodeRepository.save(otp);
    }
}

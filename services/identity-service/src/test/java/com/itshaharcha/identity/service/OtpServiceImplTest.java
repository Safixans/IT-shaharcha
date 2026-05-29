package com.itshaharcha.identity.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.OtpCode;
import com.itshaharcha.identity.entity.OtpPurpose;
import com.itshaharcha.identity.repository.OtpCodeRepository;
import com.itshaharcha.identity.service.impl.OtpServiceImpl;
import com.itshaharcha.identity.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock private OtpCodeRepository otpCodeRepository;
    @InjectMocks private OtpServiceImpl otpService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(UUID.randomUUID());
    }

    @Test
    void issue_persistsHashedCode_withFutureExpiry() {
        otpService.issue(account, OtpPurpose.EMAIL_VERIFICATION);

        ArgumentCaptor<OtpCode> captor = ArgumentCaptor.forClass(OtpCode.class);
        verify(otpCodeRepository).save(captor.capture());
        OtpCode saved = captor.getValue();
        assertThat(saved.getAccount()).isSameAs(account);
        assertThat(saved.getPurpose()).isEqualTo(OtpPurpose.EMAIL_VERIFICATION);
        assertThat(saved.getCodeHash()).hasSize(64); // sha-256 hex
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
        assertThat(saved.isConsumed()).isFalse();
    }

    @Test
    void verifyAndConsume_marksConsumed_whenCodeMatches() {
        OtpCode otp = new OtpCode();
        otp.setCodeHash(HashUtil.sha256("123456"));
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        otp.setConsumed(false);
        when(otpCodeRepository.findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
                account, OtpPurpose.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));

        otpService.verifyAndConsume(account, OtpPurpose.EMAIL_VERIFICATION, "123456");

        assertThat(otp.isConsumed()).isTrue();
        verify(otpCodeRepository).save(otp);
    }

    @Test
    void verifyAndConsume_rejectsWhenNoActiveCode() {
        when(otpCodeRepository.findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
                account, OtpPurpose.EMAIL_VERIFICATION)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.verifyAndConsume(
                account, OtpPurpose.EMAIL_VERIFICATION, "123456"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void verifyAndConsume_rejectsWrongCode() {
        OtpCode otp = new OtpCode();
        otp.setCodeHash(HashUtil.sha256("123456"));
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        when(otpCodeRepository.findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
                account, OtpPurpose.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> otpService.verifyAndConsume(
                account, OtpPurpose.EMAIL_VERIFICATION, "999999"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);

        verify(otpCodeRepository, never()).save(any());
    }

    @Test
    void verifyAndConsume_rejectsExpiredCode() {
        OtpCode otp = new OtpCode();
        otp.setCodeHash(HashUtil.sha256("123456"));
        otp.setExpiresAt(Instant.now().minusSeconds(1));
        when(otpCodeRepository.findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
                account, OtpPurpose.EMAIL_VERIFICATION)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> otpService.verifyAndConsume(
                account, OtpPurpose.EMAIL_VERIFICATION, "123456"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);
    }
}

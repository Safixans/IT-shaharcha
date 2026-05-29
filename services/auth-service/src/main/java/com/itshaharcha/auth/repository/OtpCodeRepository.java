package com.itshaharcha.auth.repository;

import com.itshaharcha.auth.entity.Account;
import com.itshaharcha.auth.entity.OtpCode;
import com.itshaharcha.auth.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            Account account, OtpPurpose purpose);
}

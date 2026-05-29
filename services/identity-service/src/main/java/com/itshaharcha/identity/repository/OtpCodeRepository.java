package com.itshaharcha.identity.repository;

import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.OtpCode;
import com.itshaharcha.identity.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findTopByAccountAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            Account account, OtpPurpose purpose);
}

package com.itshaharcha.portfolio.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.portfolio.dto.request.CertificateCreate;
import com.itshaharcha.portfolio.dto.request.VerifyInput;
import com.itshaharcha.portfolio.dto.response.CertificateResponse;
import com.itshaharcha.portfolio.dto.response.PageResponse;
import com.itshaharcha.portfolio.entity.Certificate;
import com.itshaharcha.portfolio.entity.VerificationStatus;
import com.itshaharcha.portfolio.event.PortfolioEventPublisher;
import com.itshaharcha.portfolio.mapper.PortfolioMapper;
import com.itshaharcha.portfolio.repository.CertificateRepository;
import com.itshaharcha.portfolio.repository.FileRepository;
import com.itshaharcha.portfolio.security.SecurityUtils;
import com.itshaharcha.portfolio.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final FileRepository fileRepository;
    private final PortfolioMapper mapper;
    private final PortfolioEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> list(Pageable pageable) {
        UUID accountId = SecurityUtils.currentAccountId();
        return PageResponse.from(
                certificateRepository.findByAccountId(accountId, pageable),
                mapper::toCertificateResponse);
    }

    @Override
    @Transactional
    public CertificateResponse create(CertificateCreate input) {
        UUID accountId = SecurityUtils.currentAccountId();
        if (input.fileId() != null && !fileRepository.existsById(input.fileId())) {
            throw ApplicationException.badRequest("Referenced fileId does not exist");
        }
        Certificate certificate = new Certificate();
        certificate.setAccountId(accountId);
        certificate.setTitle(input.title());
        certificate.setIssuer(input.issuer());
        certificate.setIssuedOn(input.issuedOn());
        certificate.setFileId(input.fileId());
        certificate.setCredentialUrl(input.credentialUrl());
        certificate.setStatus(VerificationStatus.PENDING);
        Certificate saved = certificateRepository.save(certificate);

        Map<String, Object> data = new HashMap<>();
        data.put("title", saved.getTitle());
        data.put("issuer", saved.getIssuer());
        events.emit("portfolio.certificate.uploaded", "certificate", saved.getId(), data);
        return mapper.toCertificateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse get(UUID certificateId) {
        UUID accountId = SecurityUtils.currentAccountId();
        Certificate certificate = certificateRepository.findByIdAndAccountId(certificateId, accountId)
                .orElseThrow(() -> ApplicationException.notFound("Certificate not found"));
        return mapper.toCertificateResponse(certificate);
    }

    @Override
    @Transactional
    public void delete(UUID certificateId) {
        UUID accountId = SecurityUtils.currentAccountId();
        Certificate certificate = certificateRepository.findByIdAndAccountId(certificateId, accountId)
                .orElseThrow(() -> ApplicationException.notFound("Certificate not found"));
        certificateRepository.delete(certificate);
    }

    @Override
    @Transactional
    public CertificateResponse verify(UUID certificateId, VerifyInput input) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> ApplicationException.notFound("Certificate not found"));
        boolean approved = input == null || input.verified() == null || input.verified();
        certificate.setStatus(approved ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
        certificate.setVerifiedAt(Instant.now());
        certificate.setVerificationNote(input == null ? null : input.note());
        Certificate saved = certificateRepository.save(certificate);

        Map<String, Object> data = new HashMap<>();
        data.put("status", saved.getStatus().name());
        events.emit("portfolio.certificate.verified", "certificate", saved.getId(), data);
        return mapper.toCertificateResponse(saved);
    }
}

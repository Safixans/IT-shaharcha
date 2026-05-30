package com.itshaharcha.portfolio.service;

import com.itshaharcha.portfolio.dto.request.CertificateCreate;
import com.itshaharcha.portfolio.dto.request.VerifyInput;
import com.itshaharcha.portfolio.dto.response.CertificateResponse;
import com.itshaharcha.portfolio.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CertificateService {

    PageResponse<CertificateResponse> list(Pageable pageable);

    CertificateResponse create(CertificateCreate input);

    CertificateResponse get(UUID certificateId);

    void delete(UUID certificateId);

    CertificateResponse verify(UUID certificateId, VerifyInput input);
}

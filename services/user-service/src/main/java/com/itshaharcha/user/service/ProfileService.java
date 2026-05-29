package com.itshaharcha.user.service;

import com.itshaharcha.user.dto.request.AddCertificateRequest;
import com.itshaharcha.user.dto.request.AddEducationRequest;
import com.itshaharcha.user.dto.request.AddPortfolioItemRequest;
import com.itshaharcha.user.dto.request.UpdateProfileRequest;
import com.itshaharcha.user.dto.response.CertificateResponse;
import com.itshaharcha.user.dto.response.EducationResponse;
import com.itshaharcha.user.dto.response.PortfolioItemResponse;
import com.itshaharcha.user.dto.response.ProfileResponse;

import java.util.UUID;

public interface ProfileService {

    /** Idempotently provisions a profile for the account (used on first access / events). */
    ProfileResponse getOrCreateProfile(UUID accountId);

    ProfileResponse getByAccountId(UUID accountId);

    ProfileResponse updateProfile(UUID accountId, UpdateProfileRequest request);

    CertificateResponse addCertificate(UUID accountId, AddCertificateRequest request);

    void deleteCertificate(UUID accountId, UUID certificateId);

    EducationResponse addEducation(UUID accountId, AddEducationRequest request);

    PortfolioItemResponse addPortfolioItem(UUID accountId, AddPortfolioItemRequest request);
}

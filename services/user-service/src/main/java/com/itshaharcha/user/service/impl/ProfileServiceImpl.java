package com.itshaharcha.user.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.user.dto.request.AddCertificateRequest;
import com.itshaharcha.user.dto.request.AddEducationRequest;
import com.itshaharcha.user.dto.request.AddPortfolioItemRequest;
import com.itshaharcha.user.dto.request.UpdateProfileRequest;
import com.itshaharcha.user.dto.response.CertificateResponse;
import com.itshaharcha.user.dto.response.EducationResponse;
import com.itshaharcha.user.dto.response.PortfolioItemResponse;
import com.itshaharcha.user.dto.response.ProfileResponse;
import com.itshaharcha.user.entity.Certificate;
import com.itshaharcha.user.entity.EducationHistory;
import com.itshaharcha.user.entity.PortfolioItem;
import com.itshaharcha.user.entity.Profile;
import com.itshaharcha.user.mapper.ProfileMapper;
import com.itshaharcha.user.repository.ProfileRepository;
import com.itshaharcha.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ProfileResponse getOrCreateProfile(UUID accountId) {
        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseGet(() -> createProfile(accountId));
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getByAccountId(UUID accountId) {
        return profileRepository.findByAccountId(accountId)
                .map(profileMapper::toResponse)
                .orElseThrow(() -> ApplicationException.notFound("Profile not found"));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID accountId, UpdateProfileRequest request) {
        Profile profile = requireProfile(accountId);
        if (request.fullName() != null) {
            profile.setFullName(request.fullName());
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.country() != null) {
            profile.setCountry(request.country());
        }
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public CertificateResponse addCertificate(UUID accountId, AddCertificateRequest request) {
        Profile profile = requireProfile(accountId);
        Certificate certificate = new Certificate();
        certificate.setTitle(request.title());
        certificate.setIssuer(request.issuer());
        certificate.setFileId(request.fileId());
        certificate.setCredentialUrl(request.credentialUrl());
        certificate.setIssuedAt(request.issuedAt());
        profile.addCertificate(certificate);
        profileRepository.save(profile);
        return profileMapper.toResponse(certificate);
    }

    @Override
    @Transactional
    public void deleteCertificate(UUID accountId, UUID certificateId) {
        Profile profile = requireProfile(accountId);
        boolean removed = profile.getCertificates()
                .removeIf(c -> c.getId().equals(certificateId));
        if (!removed) {
            throw ApplicationException.notFound("Certificate not found");
        }
    }

    @Override
    @Transactional
    public EducationResponse addEducation(UUID accountId, AddEducationRequest request) {
        Profile profile = requireProfile(accountId);
        EducationHistory education = new EducationHistory();
        education.setInstitution(request.institution());
        education.setDegree(request.degree());
        education.setFieldOfStudy(request.fieldOfStudy());
        education.setStartDate(request.startDate());
        education.setEndDate(request.endDate());
        profile.addEducation(education);
        profileRepository.save(profile);
        return profileMapper.toResponse(education);
    }

    @Override
    @Transactional
    public PortfolioItemResponse addPortfolioItem(UUID accountId, AddPortfolioItemRequest request) {
        Profile profile = requireProfile(accountId);
        PortfolioItem item = new PortfolioItem();
        item.setType(request.type());
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setUrl(request.url());
        profile.addPortfolioItem(item);
        profileRepository.save(profile);
        return profileMapper.toResponse(item);
    }

    private Profile requireProfile(UUID accountId) {
        return profileRepository.findByAccountId(accountId)
                .orElseGet(() -> createProfile(accountId));
    }

    private Profile createProfile(UUID accountId) {
        Profile profile = new Profile();
        profile.setAccountId(accountId);
        Profile saved = profileRepository.save(profile);
        log.info("Provisioned profile for account {}", accountId);
        return saved;
    }
}

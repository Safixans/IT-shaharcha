package com.itshaharcha.user.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
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
import com.itshaharcha.user.entity.PortfolioItemType;
import com.itshaharcha.user.entity.Profile;
import com.itshaharcha.user.mapper.ProfileMapper;
import com.itshaharcha.user.repository.ProfileRepository;
import com.itshaharcha.user.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private ProfileMapper profileMapper;

    @InjectMocks private ProfileServiceImpl profileService;

    private UUID accountId;
    private Profile profile;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setAccountId(accountId);
    }

    @Test
    void getOrCreateProfile_createsWhenMissing() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileMapper.toResponse(any(Profile.class))).thenReturn(
                new ProfileResponse(UUID.randomUUID(), accountId, null, null, null, null,
                        List.of(), List.of(), List.of()));

        ProfileResponse response = profileService.getOrCreateProfile(accountId);

        assertThat(response.accountId()).isEqualTo(accountId);
    }

    @Test
    void updateProfile_appliesNonNullFields() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(
                new ProfileResponse(profile.getId(), accountId, "Jane Doe", null, "hi", "UZ",
                        List.of(), List.of(), List.of()));

        profileService.updateProfile(accountId,
                new UpdateProfileRequest("Jane Doe", null, "hi", "UZ"));

        assertThat(profile.getFullName()).isEqualTo("Jane Doe");
        assertThat(profile.getBio()).isEqualTo("hi");
        assertThat(profile.getCountry()).isEqualTo("UZ");
    }

    @Test
    void addCertificate_attachesToProfile() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileMapper.toResponse(any(Certificate.class))).thenReturn(
                new CertificateResponse(UUID.randomUUID(), "AWS", "Amazon", null, null, null));

        CertificateResponse response = profileService.addCertificate(accountId,
                new AddCertificateRequest("AWS", "Amazon", null, null, null));

        assertThat(response.title()).isEqualTo("AWS");
        assertThat(profile.getCertificates()).hasSize(1);
    }

    @Test
    void deleteCertificate_throwsWhenNotFound() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileService.deleteCertificate(accountId, UUID.randomUUID()))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getOrCreateProfile_returnsExisting_withoutSaving() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(
                new ProfileResponse(profile.getId(), accountId, null, null, null, null,
                        List.of(), List.of(), List.of()));

        ProfileResponse response = profileService.getOrCreateProfile(accountId);

        assertThat(response.accountId()).isEqualTo(accountId);
        verify(profileRepository, never()).save(any());
    }

    @Test
    void getByAccountId_returnsProfile_whenPresent() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(
                new ProfileResponse(profile.getId(), accountId, "Jane", null, null, null,
                        List.of(), List.of(), List.of()));

        assertThat(profileService.getByAccountId(accountId).fullName()).isEqualTo("Jane");
    }

    @Test
    void getByAccountId_throwsNotFound_whenMissing() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getByAccountId(accountId))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void updateProfile_appliesAvatar_andLeavesNullsUntouched() {
        profile.setFullName("Existing");
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(
                new ProfileResponse(profile.getId(), accountId, "Existing",
                        "http://img", null, null, List.of(), List.of(), List.of()));

        profileService.updateProfile(accountId,
                new UpdateProfileRequest(null, "http://img", null, null));

        assertThat(profile.getFullName()).isEqualTo("Existing"); // untouched
        assertThat(profile.getAvatarUrl()).isEqualTo("http://img");
    }

    @Test
    void updateProfile_createsProfile_whenMissing() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileMapper.toResponse(any(Profile.class))).thenReturn(
                new ProfileResponse(UUID.randomUUID(), accountId, "Jane", null, null, null,
                        List.of(), List.of(), List.of()));

        profileService.updateProfile(accountId,
                new UpdateProfileRequest("Jane", null, null, null));

        verify(profileRepository).save(any(Profile.class)); // provisioned
    }

    @Test
    void addEducation_attachesToProfile() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileMapper.toResponse(any(EducationHistory.class))).thenReturn(
                new EducationResponse(UUID.randomUUID(), "MIT", "BSc", "CS", null, null));

        EducationResponse response = profileService.addEducation(accountId,
                new AddEducationRequest("MIT", "BSc", "CS", null, null));

        assertThat(response.institution()).isEqualTo("MIT");
        assertThat(profile.getEducationHistory()).hasSize(1);
    }

    @Test
    void addPortfolioItem_attachesToProfile() {
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileMapper.toResponse(any(PortfolioItem.class))).thenReturn(
                new PortfolioItemResponse(UUID.randomUUID(), PortfolioItemType.PROJECT,
                        "App", "desc", "http://x"));

        PortfolioItemResponse response = profileService.addPortfolioItem(accountId,
                new AddPortfolioItemRequest(PortfolioItemType.PROJECT, "App", "desc", "http://x"));

        assertThat(response.title()).isEqualTo("App");
        assertThat(profile.getPortfolioItems()).hasSize(1);
    }

    @Test
    void deleteCertificate_removesMatchingCertificate() {
        Certificate cert = new Certificate();
        cert.setId(UUID.randomUUID());
        cert.setTitle("AWS");
        profile.addCertificate(cert);
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        profileService.deleteCertificate(accountId, cert.getId());

        assertThat(profile.getCertificates()).isEmpty();
    }
}

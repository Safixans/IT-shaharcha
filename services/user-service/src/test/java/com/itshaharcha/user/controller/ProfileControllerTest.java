package com.itshaharcha.user.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.user.dto.request.AddCertificateRequest;
import com.itshaharcha.user.dto.request.AddEducationRequest;
import com.itshaharcha.user.dto.request.AddPortfolioItemRequest;
import com.itshaharcha.user.dto.request.UpdateProfileRequest;
import com.itshaharcha.user.dto.response.CertificateResponse;
import com.itshaharcha.user.dto.response.EducationResponse;
import com.itshaharcha.user.dto.response.PortfolioItemResponse;
import com.itshaharcha.user.dto.response.ProfileResponse;
import com.itshaharcha.user.entity.PortfolioItemType;
import com.itshaharcha.user.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock private ProfileService profileService;
    @InjectMocks private ProfileController controller;

    private final UUID accountId = UUID.randomUUID();

    @Test
    void myProfile_delegates() {
        var profile = mock(ProfileResponse.class);
        when(profileService.getOrCreateProfile(accountId)).thenReturn(profile);

        assertThat(controller.myProfile(accountId).data()).isSameAs(profile);
        verify(profileService).getOrCreateProfile(accountId);
    }

    @Test
    void updateMyProfile_delegates() {
        var req = new UpdateProfileRequest("Jane", null, "bio", "UZ");
        var profile = mock(ProfileResponse.class);
        when(profileService.updateProfile(accountId, req)).thenReturn(profile);

        assertThat(controller.updateMyProfile(accountId, req).data()).isSameAs(profile);
        verify(profileService).updateProfile(accountId, req);
    }

    @Test
    void profile_byAccountId_delegates() {
        UUID other = UUID.randomUUID();
        var profile = mock(ProfileResponse.class);
        when(profileService.getByAccountId(other)).thenReturn(profile);

        assertThat(controller.profile(other).data()).isSameAs(profile);
        verify(profileService).getByAccountId(other);
    }

    @Test
    void addCertificate_delegates() {
        var req = new AddCertificateRequest("AWS", "Amazon", null, null, null);
        var cert = mock(CertificateResponse.class);
        when(profileService.addCertificate(accountId, req)).thenReturn(cert);

        assertThat(controller.addCertificate(accountId, req).data()).isSameAs(cert);
        verify(profileService).addCertificate(accountId, req);
    }

    @Test
    void deleteCertificate_delegatesAndReturnsMessage() {
        UUID certId = UUID.randomUUID();

        ApiResponse<Void> res = controller.deleteCertificate(accountId, certId);

        assertThat(res.message()).contains("Certificate removed");
        verify(profileService).deleteCertificate(accountId, certId);
    }

    @Test
    void addEducation_delegates() {
        var req = new AddEducationRequest("MIT", "BSc", "CS", null, null);
        var edu = mock(EducationResponse.class);
        when(profileService.addEducation(accountId, req)).thenReturn(edu);

        assertThat(controller.addEducation(accountId, req).data()).isSameAs(edu);
        verify(profileService).addEducation(accountId, req);
    }

    @Test
    void addPortfolioItem_delegates() {
        var req = new AddPortfolioItemRequest(PortfolioItemType.PROJECT, "App", null, null);
        var item = mock(PortfolioItemResponse.class);
        when(profileService.addPortfolioItem(accountId, req)).thenReturn(item);

        assertThat(controller.addPortfolioItem(accountId, req).data()).isSameAs(item);
        verify(profileService).addPortfolioItem(accountId, req);
    }
}

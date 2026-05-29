package com.itshaharcha.identity.controller;

import com.itshaharcha.identity.dto.request.ProfileUpdate;
import com.itshaharcha.identity.dto.response.ProfileResponse;
import com.itshaharcha.identity.service.ProfileService;
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

    @Test
    void me_delegatesPrincipal() {
        UUID accountId = UUID.randomUUID();
        var profile = mock(ProfileResponse.class);
        when(profileService.getMyProfile(accountId)).thenReturn(profile);

        assertThat(controller.me(accountId).data()).isSameAs(profile);
        verify(profileService).getMyProfile(accountId);
    }

    @Test
    void update_delegatesPrincipalAndBody() {
        UUID accountId = UUID.randomUUID();
        var request = new ProfileUpdate("Jane", null, null, null, null, null);
        var profile = mock(ProfileResponse.class);
        when(profileService.updateMyProfile(accountId, request)).thenReturn(profile);

        assertThat(controller.update(accountId, request).data()).isSameAs(profile);
        verify(profileService).updateMyProfile(accountId, request);
    }
}

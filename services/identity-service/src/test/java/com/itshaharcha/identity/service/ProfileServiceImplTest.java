package com.itshaharcha.identity.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.identity.dto.request.ProfileLinkDto;
import com.itshaharcha.identity.dto.request.ProfileUpdate;
import com.itshaharcha.identity.dto.response.ProfileResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.Profile;
import com.itshaharcha.identity.entity.ProfileLink;
import com.itshaharcha.identity.mapper.ProfileMapper;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.ProfileRepository;
import com.itshaharcha.identity.service.impl.ProfileServiceImpl;
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
    @Mock private AccountRepository accountRepository;
    @Mock private ProfileMapper profileMapper;
    @InjectMocks private ProfileServiceImpl profileService;

    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setId(accountId);
        account.setUsername("jane");
    }

    private void stubAccountFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    }

    @Test
    void getMyProfile_returnsExistingProfile() {
        stubAccountFound();
        Profile profile = new Profile();
        profile.setAccountId(accountId);
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        var mapped = new ProfileResponse(accountId, "jane", null, null, null, null, null, List.of(), null);
        when(profileMapper.toResponse(profile, "jane")).thenReturn(mapped);

        ProfileResponse response = profileService.getMyProfile(accountId);

        assertThat(response.username()).isEqualTo("jane");
        verify(profileRepository, never()).save(any());
    }

    @Test
    void getMyProfile_lazilyCreatesProfile_whenMissing() {
        stubAccountFound();
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        Profile created = new Profile();
        created.setAccountId(accountId);
        when(profileRepository.save(any(Profile.class))).thenReturn(created);
        when(profileMapper.toResponse(created, "jane")).thenReturn(
                new ProfileResponse(accountId, "jane", null, null, null, null, null, List.of(), null));

        profileService.getMyProfile(accountId);

        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void getMyProfile_throwsWhenAccountMissing() {
        UUID ghost = UUID.randomUUID();
        when(accountRepository.findById(ghost)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile(ghost))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void updateMyProfile_appliesOnlyNonNullFields() {
        stubAccountFound();
        Profile profile = new Profile();
        profile.setAccountId(accountId);
        profile.setBio("old bio");
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(any(Profile.class), any())).thenReturn(
                new ProfileResponse(accountId, "jane", "Jane Doe", "old bio", null, null, "UZ", List.of(), null));

        var request = new ProfileUpdate("Jane Doe", null, null, null, "UZ", null);
        profileService.updateMyProfile(accountId, request);

        assertThat(profile.getFullName()).isEqualTo("Jane Doe");
        assertThat(profile.getCountry()).isEqualTo("UZ");
        assertThat(profile.getBio()).isEqualTo("old bio"); // unchanged (null in request)
    }

    @Test
    void updateMyProfile_replacesLinks_whenProvided() {
        stubAccountFound();
        Profile profile = new Profile();
        profile.setAccountId(accountId);
        profile.getLinks().add(new ProfileLink("Old", "https://old.example"));
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toEntity(any(ProfileLinkDto.class)))
                .thenReturn(new ProfileLink("GitHub", "https://github.com/jane"));
        when(profileMapper.toResponse(any(Profile.class), any())).thenReturn(
                new ProfileResponse(accountId, "jane", null, null, null, null, null, List.of(), null));

        var request = new ProfileUpdate(null, null, null, null, null,
                List.of(new ProfileLinkDto("GitHub", "https://github.com/jane")));
        profileService.updateMyProfile(accountId, request);

        assertThat(profile.getLinks()).hasSize(1);
        assertThat(profile.getLinks().get(0).getLabel()).isEqualTo("GitHub");
    }

    @Test
    void updateMyProfile_leavesLinksUntouched_whenNull() {
        stubAccountFound();
        Profile profile = new Profile();
        profile.setAccountId(accountId);
        profile.getLinks().add(new ProfileLink("GitHub", "https://github.com/jane"));
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(any(Profile.class), any())).thenReturn(
                new ProfileResponse(accountId, "jane", null, null, null, null, null, List.of(), null));

        var request = new ProfileUpdate("Jane", null, null, null, null, null);
        profileService.updateMyProfile(accountId, request);

        assertThat(profile.getLinks()).hasSize(1);
    }
}

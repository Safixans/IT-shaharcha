package com.itshaharcha.identity.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.identity.dto.request.ProfileUpdate;
import com.itshaharcha.identity.dto.response.ProfileResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.Profile;
import com.itshaharcha.identity.mapper.ProfileMapper;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.ProfileRepository;
import com.itshaharcha.identity.service.ProfileService;
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
    private final AccountRepository accountRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ProfileResponse getMyProfile(UUID accountId) {
        Account account = requireAccount(accountId);
        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseGet(() -> createProfile(accountId));
        return profileMapper.toResponse(profile, account.getUsername());
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(UUID accountId, ProfileUpdate request) {
        Account account = requireAccount(accountId);
        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseGet(() -> createProfile(accountId));

        if (request.fullName() != null) {
            profile.setFullName(request.fullName());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }
        if (request.locale() != null) {
            profile.setLocale(request.locale());
        }
        if (request.country() != null) {
            profile.setCountry(request.country());
        }
        if (request.links() != null) {
            profile.getLinks().clear();
            request.links().forEach(dto -> profile.getLinks().add(profileMapper.toEntity(dto)));
        }
        return profileMapper.toResponse(profile, account.getUsername());
    }

    private Account requireAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));
    }

    private Profile createProfile(UUID accountId) {
        Profile profile = new Profile();
        profile.setAccountId(accountId);
        Profile saved = profileRepository.save(profile);
        log.info("Provisioned profile for account {}", accountId);
        return saved;
    }
}

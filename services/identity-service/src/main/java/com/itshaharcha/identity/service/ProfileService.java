package com.itshaharcha.identity.service;

import com.itshaharcha.identity.dto.request.ProfileUpdate;
import com.itshaharcha.identity.dto.response.ProfileResponse;

import java.util.UUID;

public interface ProfileService {

    ProfileResponse getMyProfile(UUID accountId);

    ProfileResponse updateMyProfile(UUID accountId, ProfileUpdate request);
}

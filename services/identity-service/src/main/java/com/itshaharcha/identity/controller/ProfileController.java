package com.itshaharcha.identity.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.request.ProfileUpdate;
import com.itshaharcha.identity.dto.response.ProfileResponse;
import com.itshaharcha.identity.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Profile", description = "Current account's own profile")
@RestController
@RequestMapping("/api/v1/identity/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get the authenticated account's profile")
    @GetMapping
    public ApiResponse<ProfileResponse> me(@AuthenticationPrincipal UUID accountId) {
        return ApiResponse.ok(profileService.getMyProfile(accountId));
    }

    @Operation(summary = "Update the authenticated account's profile")
    @PatchMapping
    public ApiResponse<ProfileResponse> update(@AuthenticationPrincipal UUID accountId,
                                               @Valid @RequestBody ProfileUpdate request) {
        return ApiResponse.ok(profileService.updateMyProfile(accountId, request));
    }
}

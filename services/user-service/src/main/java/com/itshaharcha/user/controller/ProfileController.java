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
import com.itshaharcha.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Profiles", description = "Student/teacher profiles, certificates, education, portfolio")
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get (or lazily create) the current user's profile")
    @GetMapping("/me")
    public ApiResponse<ProfileResponse> myProfile(@AuthenticationPrincipal UUID accountId) {
        return ApiResponse.ok(profileService.getOrCreateProfile(accountId));
    }

    @Operation(summary = "Update the current user's profile")
    @PutMapping("/me")
    public ApiResponse<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(profileService.updateProfile(accountId, request));
    }

    @Operation(summary = "View a profile by account id")
    @GetMapping("/{accountId}")
    public ApiResponse<ProfileResponse> profile(@PathVariable UUID accountId) {
        return ApiResponse.ok(profileService.getByAccountId(accountId));
    }

    @Operation(summary = "Add a certificate to the current user's portfolio")
    @PostMapping("/me/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CertificateResponse> addCertificate(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody AddCertificateRequest request) {
        return ApiResponse.ok(profileService.addCertificate(accountId, request));
    }

    @Operation(summary = "Remove a certificate")
    @DeleteMapping("/me/certificates/{certificateId}")
    public ApiResponse<Void> deleteCertificate(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID certificateId) {
        profileService.deleteCertificate(accountId, certificateId);
        return ApiResponse.message("Certificate removed.");
    }

    @Operation(summary = "Add an education history entry")
    @PostMapping("/me/education")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EducationResponse> addEducation(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody AddEducationRequest request) {
        return ApiResponse.ok(profileService.addEducation(accountId, request));
    }

    @Operation(summary = "Add a portfolio item")
    @PostMapping("/me/portfolio")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PortfolioItemResponse> addPortfolioItem(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody AddPortfolioItemRequest request) {
        return ApiResponse.ok(profileService.addPortfolioItem(accountId, request));
    }
}

package com.itshaharcha.auth.controller;

import com.itshaharcha.auth.dto.request.LoginRequest;
import com.itshaharcha.auth.dto.request.RefreshTokenRequest;
import com.itshaharcha.auth.dto.request.RegisterRequest;
import com.itshaharcha.auth.dto.request.VerifyOtpRequest;
import com.itshaharcha.auth.dto.response.AccountResponse;
import com.itshaharcha.auth.dto.response.AuthResponse;
import com.itshaharcha.auth.service.AuthService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Tag(name = "Authentication", description = "Registration, login, tokens, verification")
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new student account")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request),
                "Registered. Check your email for the verification code.");
    }

    @Operation(summary = "Authenticate and receive access/refresh tokens")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "Rotate a refresh token for a new token pair")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @Operation(summary = "Verify an account with the emailed OTP")
    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ApiResponse.message("Account verified. You can now log in.");
    }

    @Operation(summary = "Resend the email verification OTP")
    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendOtp(@RequestParam @NotBlank @Email String email) {
        authService.resendOtp(email);
        return ApiResponse.message("A new verification code has been sent.");
    }

    @Operation(summary = "Revoke a refresh token (logout)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.message("Logged out.");
    }

    @Operation(summary = "Current authenticated account")
    @GetMapping("/me")
    public ApiResponse<AccountResponse> me(@AuthenticationPrincipal UUID accountId) {
        return ApiResponse.ok(authService.currentAccount(accountId));
    }
}

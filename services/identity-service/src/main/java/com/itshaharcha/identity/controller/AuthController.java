package com.itshaharcha.identity.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.request.LoginRequest;
import com.itshaharcha.identity.dto.request.RefreshRequest;
import com.itshaharcha.identity.dto.request.RegisterRequest;
import com.itshaharcha.identity.dto.request.ResendOtpRequest;
import com.itshaharcha.identity.dto.request.VerifyRequest;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.TokenPair;
import com.itshaharcha.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Registration, login, tokens, verification")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new account")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request),
                "Registered. Check your email for the verification code.");
    }

    @Operation(summary = "Authenticate and receive an access/refresh token pair")
    @PostMapping("/login")
    public ApiResponse<TokenPair> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "Rotate a refresh token for a new token pair")
    @PostMapping("/refresh")
    public ApiResponse<TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @Operation(summary = "Verify an account with the emailed OTP")
    @PostMapping("/verify")
    public ApiResponse<AccountResponse> verify(@Valid @RequestBody VerifyRequest request) {
        return ApiResponse.ok(authService.verify(request),
                "Account verified. You can now log in.");
    }

    @Operation(summary = "Resend the email verification OTP")
    @PostMapping("/otp:resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.email());
        return ApiResponse.message("If the account exists, a new verification code has been sent.");
    }

    @Operation(summary = "Revoke a refresh token (logout)")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) RefreshRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
    }
}

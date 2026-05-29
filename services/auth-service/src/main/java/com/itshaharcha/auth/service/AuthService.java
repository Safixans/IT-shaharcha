package com.itshaharcha.auth.service;

import com.itshaharcha.auth.dto.request.LoginRequest;
import com.itshaharcha.auth.dto.request.RefreshTokenRequest;
import com.itshaharcha.auth.dto.request.RegisterRequest;
import com.itshaharcha.auth.dto.request.VerifyOtpRequest;
import com.itshaharcha.auth.dto.response.AccountResponse;
import com.itshaharcha.auth.dto.response.AuthResponse;

import java.util.UUID;

public interface AuthService {

    AccountResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void resendOtp(String email);

    void logout(String refreshToken);

    AccountResponse currentAccount(UUID accountId);
}

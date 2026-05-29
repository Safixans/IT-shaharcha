package com.itshaharcha.identity.service;

import com.itshaharcha.identity.dto.request.LoginRequest;
import com.itshaharcha.identity.dto.request.RefreshRequest;
import com.itshaharcha.identity.dto.request.RegisterRequest;
import com.itshaharcha.identity.dto.request.VerifyRequest;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.TokenPair;

public interface AuthService {

    AccountResponse register(RegisterRequest request);

    TokenPair login(LoginRequest request);

    TokenPair refresh(RefreshRequest request);

    AccountResponse verify(VerifyRequest request);

    void resendOtp(String email);

    void logout(String refreshToken);
}

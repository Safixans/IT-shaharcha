package com.itshaharcha.identity.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** SHA-256 hashing for opaque secrets (refresh tokens, OTP codes) and OTP generation. */
public final class HashUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private HashUtil() {
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Generates a zero-padded 6-digit numeric OTP. */
    public static String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}

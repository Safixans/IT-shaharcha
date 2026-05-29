package com.itshaharcha.common.exception;

import lombok.Getter;

/** Base domain exception carrying a stable {@link ErrorCode}. */
@Getter
public class ApplicationException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public static ApplicationException notFound(String message) {
        return new ApplicationException(ErrorCode.NOT_FOUND, message);
    }

    public static ApplicationException badRequest(String message) {
        return new ApplicationException(ErrorCode.BAD_REQUEST, message);
    }

    public static ApplicationException conflict(String message) {
        return new ApplicationException(ErrorCode.CONFLICT, message);
    }

    public static ApplicationException unauthorized(String message) {
        return new ApplicationException(ErrorCode.UNAUTHORIZED, message);
    }

    public static ApplicationException forbidden(String message) {
        return new ApplicationException(ErrorCode.FORBIDDEN, message);
    }
}

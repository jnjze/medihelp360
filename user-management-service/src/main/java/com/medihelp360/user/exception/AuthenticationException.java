package com.medihelp360.user.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_001";
        this.httpStatus = 401;
    }
    
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 401;
    }
    
    public AuthenticationException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_001";
        this.httpStatus = 401;
    }
}

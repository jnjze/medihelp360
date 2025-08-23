package com.medihelp360.user.exception;

import lombok.Getter;

@Getter
public class RegistrationException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public RegistrationException(String message) {
        super(message);
        this.errorCode = "REG_001";
        this.httpStatus = 400;
    }
    
    public RegistrationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 400;
    }
    
    public RegistrationException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "REG_001";
        this.httpStatus = 400;
    }
    
    // Códigos de error específicos para registro
    public static final String EMAIL_ALREADY_EXISTS = "REG_002";
    public static final String PASSWORD_MISMATCH = "REG_003";
    public static final String INVALID_PASSWORD_FORMAT = "REG_004";
    public static final String ROLE_NOT_FOUND = "REG_005";
}

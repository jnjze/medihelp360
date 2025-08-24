package com.medihelp360.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    private String error;
    private String message;
    private String path;
    
    // Campos opcionales para errores de validación
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationError> validationErrors;
    
    // Campo opcional para errores de negocio
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;
    
    // Campo opcional para detalles adicionales
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> details;
    
    // Campo opcional para sugerencias de solución
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String suggestion;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private String rejectedValue;
    }
    
    // Factory methods para diferentes tipos de errores
    public static ErrorResponse badRequest(String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Bad Request")
            .message(message)
            .path(path)
            .build();
    }
    
    public static ErrorResponse validationError(String message, String path, List<ValidationError> errors) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Validation Error")
            .message(message)
            .path(path)
            .validationErrors(errors)
            .suggestion("Please check the validation errors and try again")
            .build();
    }
    
    public static ErrorResponse authenticationError(String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(401)
            .error("Authentication Error")
            .message(message)
            .path(path)
            .errorCode("AUTH_001")
            .suggestion("Please check your credentials and try again")
            .build();
    }
    
    public static ErrorResponse registrationError(String message, String path, String errorCode) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Registration Error")
            .message(message)
            .path(path)
            .errorCode(errorCode)
            .suggestion("Please check your registration data and try again")
            .build();
    }
    
    public static ErrorResponse internalError(String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(500)
            .error("Internal Server Error")
            .message(message)
            .path(path)
            .errorCode("SYS_001")
            .suggestion("Please try again later or contact support if the problem persists")
            .build();
    }
    
    public static ErrorResponse unauthorized(String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(401)
            .error("Unauthorized")
            .message(message)
            .path(path)
            .errorCode("AUTH_002")
            .suggestion("Please provide valid authentication credentials")
            .build();
    }
}

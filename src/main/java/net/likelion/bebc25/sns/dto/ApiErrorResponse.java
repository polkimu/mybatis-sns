package net.likelion.bebc25.sns.dto;

import net.likelion.bebc25.sns.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        int status,
        LocalDateTime timestamp,
        List<FieldErrorDetail> errors
) {

    public record FieldErrorDetail(
            String field,
            String rejectedValue,
            String reason
    ) {
    }

    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getHttpStatus().value(),
                LocalDateTime.now(),
                List.of()
        );
    }

    public static ApiErrorResponse of(
            ErrorCode errorCode,
            String message
    ) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                message,
                errorCode.getHttpStatus().value(),
                LocalDateTime.now(),
                List.of()
        );
    }

    public static ApiErrorResponse of(
            ErrorCode errorCode,
            List<FieldErrorDetail> errors
    ) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getHttpStatus().value(),
                LocalDateTime.now(),
                errors
        );
    }
}
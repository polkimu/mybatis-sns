package net.likelion.bebc25.sns.exception;

import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.sns.dto.ApiErrorResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler {

    // @Valid 유효성 검증 실패할 경우에 호출됨(400 Bad Request 응답)
    // 클라이언트가 전송한 DTO의 제약조건(@NotNull, @NotBlank, @Size 등)을 위반할 경우 스프링이 발생시키는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        // 스프링이 만든 fieldError
        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldErrorDetail(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors);
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus()).body(response);
    }


    // 2. 비즈니스 규칙 위반 → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(
            IllegalArgumentException ex
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.of(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(ErrorCode.BUSINESS_RULE_VIOLATION.getHttpStatus())
                .body(response);
    }


    // 3. 게시글 없음 → 404 ⭐
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(
            NoSuchElementException ex
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.of(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(response);
    }


    // 4. 권한 없음 → 403
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(
            IllegalStateException ex
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.of(
                        ErrorCode.FORBIDDEN_OPERATION,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN_OPERATION.getHttpStatus())
                .body(response);
    }


    // 5. 기타 서버 오류 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception ex
    ) {

        log.error("서버 내부 오류 발생", ex);

        ApiErrorResponse response =
                ApiErrorResponse.of(
                        ErrorCode.INTERNAL_SERVER_ERROR
                );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex
    ) {

        ApiErrorResponse response =
                ApiErrorResponse.of(ErrorCode.FORBIDDEN_OPERATION);

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN_OPERATION.getHttpStatus())
                .body(response);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex
    ) {
        ApiErrorResponse response =
                ApiErrorResponse.of(
                        ErrorCode.UNAUTHORIZED_ACCESS,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED_ACCESS.getHttpStatus())
                .body(response);
    }
}
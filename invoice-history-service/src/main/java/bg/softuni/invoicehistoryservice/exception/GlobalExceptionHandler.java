package bg.softuni.invoicehistoryservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidApiKeyException(InvalidApiKeyException exception,
                                                                         HttpServletRequest request) {
        log.warn("Invalid API key request handled: code={}, path={}", exception.getErrorCode(), request.getRequestURI());
        ErrorResponseDTO errorResponseDTO = buildErrorResponse(
                exception.getHttpStatus(),
                exception.getErrorCode(),
                exception.getErrorTitle(),
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus()).body(errorResponseDTO);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationException(ApplicationException exception,
                                                                      HttpServletRequest request) {
        log.warn("Application exception handled: code={}, path={}, message={}",
                exception.getErrorCode(),
                request.getRequestURI(),
                exception.getMessage());
        ErrorResponseDTO errorResponseDTO = buildErrorResponse(
                exception.getHttpStatus(),
                exception.getErrorCode(),
                exception.getErrorTitle(),
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus()).body(errorResponseDTO);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        log.warn("Validation exception handled: path={}", request.getRequestURI());
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Invalid request data");

        ErrorResponseDTO errorResponseDTO = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "validation_error",
                "Validation Error",
                message,
                request.getRequestURI());

        return ResponseEntity.badRequest().body(errorResponseDTO);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        log.warn("Invalid JSON request handled: path={}", request.getRequestURI());
        ErrorResponseDTO errorResponseDTO = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "invalid_json_request",
                "Invalid JSON Request",
                "Request body is missing or contains invalid JSON",
                request.getRequestURI());

        return ResponseEntity.badRequest().body(errorResponseDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception exception,
                                                           HttpServletRequest request) {
        log.error("Unexpected exception handled: path={}", request.getRequestURI(), exception);
        ErrorResponseDTO errorResponseDTO = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal_server_error",
                "Internal Server Error",
                "Unexpected server error",
                request.getRequestURI());

        return ResponseEntity.internalServerError().body(errorResponseDTO);
    }

    private ErrorResponseDTO buildErrorResponse(HttpStatus httpStatus,
                                                String errorCode,
                                                String errorTitle,
                                                String message,
                                                String path) {
        return ErrorResponseDTO.builder()
                .status(httpStatus.value())
                .errorCode(errorCode)
                .errorTitle(errorTitle)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

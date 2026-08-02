package bg.softuni.invoicehistoryservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationException(ApplicationException exception,
                                                                      HttpServletRequest request) {
        ErrorResponseDTO errorResponseDTO = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getErrorCode(),
                exception.getErrorTitle(),
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDTO);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
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

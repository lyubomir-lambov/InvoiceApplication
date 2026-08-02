package bg.softuni.invoiceapplication.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public String handleApplicationException(ApplicationException exception,
                                             HttpServletRequest request,
                                             HttpServletResponse response,
                                             Model model) {
        addErrorAttributes(
                model,
                response,
                exception.getHttpStatus(),
                exception.getErrorCode(),
                exception.getErrorTitle(),
                exception.getMessage(),
                request.getRequestURI());

        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException exception,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Model model) {
        addErrorAttributes(
                model,
                response,
                HttpStatus.BAD_REQUEST,
                "invalid_request",
                "Invalid Request",
                exception.getMessage(),
                request.getRequestURI());

        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           Model model) {
        addErrorAttributes(
                model,
                response,
                HttpStatus.BAD_REQUEST,
                "invalid_url_parameter",
                "Invalid URL Parameter",
                "The requested page contains an invalid parameter",
                request.getRequestURI());

        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException exception,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              Model model) {
        addErrorAttributes(
                model,
                response,
                HttpStatus.FORBIDDEN,
                "access_denied",
                "Access Denied",
                "You do not have permission to access this page",
                request.getRequestURI());

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  Model model) {
        addErrorAttributes(
                model,
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal_server_error",
                "Internal Server Error",
                "Unexpected application error",
                request.getRequestURI());

        return "error";
    }

    private void addErrorAttributes(Model model,
                                    HttpServletResponse response,
                                    HttpStatus httpStatus,
                                    String errorCode,
                                    String errorTitle,
                                    String message,
                                    String path) {
        response.setStatus(httpStatus.value());
        model.addAttribute("status", httpStatus.value());
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("message", message);
        model.addAttribute("path", path);
    }
}

package bg.softuni.invoiceapplication.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private static final String REQUEST_URI = "/invoices/not-valid";

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleApplicationException_shouldAddApplicationErrorAttributes() {
        Model model = new ExtendedModelMap();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ApplicationException exception = new ApplicationException(
                "Invoice was not found",
                "invoice_not_found",
                "Invoice Not Found",
                HttpStatus.NOT_FOUND);

        String viewName = exceptionHandler.handleApplicationException(
                exception,
                createRequest(),
                response,
                model);

        assertErrorView(viewName, response, model, HttpStatus.NOT_FOUND,
                "invoice_not_found", "Invoice Not Found", "Invoice was not found");
    }

    @Test
    void handleIllegalArgumentException_shouldAddBadRequestErrorAttributes() {
        Model model = new ExtendedModelMap();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String viewName = exceptionHandler.handleIllegalArgumentException(
                new IllegalArgumentException("Invalid invoice id"),
                createRequest(),
                response,
                model);

        assertErrorView(viewName, response, model, HttpStatus.BAD_REQUEST,
                "invalid_request", "Invalid Request", "Invalid invoice id");
    }

    @Test
    void handleMethodArgumentTypeMismatchException_shouldAddInvalidUrlParameterAttributes() {
        Model model = new ExtendedModelMap();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String viewName = exceptionHandler.handleMethodArgumentTypeMismatchException(
                null,
                createRequest(),
                response,
                model);

        assertErrorView(viewName, response, model, HttpStatus.BAD_REQUEST,
                "invalid_url_parameter", "Invalid URL Parameter", "The requested page contains an invalid parameter");
    }

    @Test
    void handleAccessDeniedException_shouldAddForbiddenErrorAttributes() {
        Model model = new ExtendedModelMap();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String viewName = exceptionHandler.handleAccessDeniedException(
                new AccessDeniedException("Forbidden"),
                createRequest(),
                response,
                model);

        assertErrorView(viewName, response, model, HttpStatus.FORBIDDEN,
                "access_denied", "Access Denied", "You do not have permission to access this page");
    }

    @Test
    void handleException_shouldAddInternalServerErrorAttributes() {
        Model model = new ExtendedModelMap();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String viewName = exceptionHandler.handleException(
                new RuntimeException("Unexpected"),
                createRequest(),
                response,
                model);

        assertErrorView(viewName, response, model, HttpStatus.INTERNAL_SERVER_ERROR,
                "internal_server_error", "Internal Server Error", "Unexpected application error");
    }

    private static MockHttpServletRequest createRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);
        return request;
    }

    private static void assertErrorView(String viewName,
                                        MockHttpServletResponse response,
                                        Model model,
                                        HttpStatus httpStatus,
                                        String errorCode,
                                        String errorTitle,
                                        String message) {
        assertThat(viewName).isEqualTo("error");
        assertThat(response.getStatus()).isEqualTo(httpStatus.value());
        assertThat(model.getAttribute("status")).isEqualTo(httpStatus.value());
        assertThat(model.getAttribute("errorCode")).isEqualTo(errorCode);
        assertThat(model.getAttribute("errorTitle")).isEqualTo(errorTitle);
        assertThat(model.getAttribute("message")).isEqualTo(message);
        assertThat(model.getAttribute("path")).isEqualTo(REQUEST_URI);
    }
}

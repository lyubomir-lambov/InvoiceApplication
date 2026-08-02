package bg.softuni.invoicehistoryservice.config;

import bg.softuni.invoicehistoryservice.exception.InvalidApiKeyException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationFilterTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "lambi-invoice-history-api-key";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorized_whenApiKeyIsMissing() throws ServletException, IOException {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(VALID_API_KEY, testExceptionResolver());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invoice-history/invoices/11111111-1111-1111-1111-111111111111");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"errorCode\":\"missing_api_key\"");
        assertThat(filterChain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldReturnForbidden_whenApiKeyIsInvalid() throws ServletException, IOException {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(VALID_API_KEY, testExceptionResolver());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invoice-history/invoices/11111111-1111-1111-1111-111111111111");
        request.addHeader(API_KEY_HEADER, "wrong-api-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"errorCode\":\"invalid_api_key\"");
        assertThat(filterChain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldAuthenticateAndContinue_whenApiKeyIsValid() throws ServletException, IOException {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(VALID_API_KEY, testExceptionResolver());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/invoice-history/invoices/11111111-1111-1111-1111-111111111111");
        request.addHeader(API_KEY_HEADER, VALID_API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(ApiKeyAuthentication.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
    }

    private HandlerExceptionResolver testExceptionResolver() {
        return (request, response, handler, exception) -> {
            InvalidApiKeyException invalidApiKeyException = (InvalidApiKeyException) exception;
            response.setStatus(invalidApiKeyException.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            try {
                response.getWriter().write("""
                        {"errorCode":"%s","errorTitle":"%s","message":"%s"}
                        """.formatted(
                        invalidApiKeyException.getErrorCode(),
                        invalidApiKeyException.getErrorTitle(),
                        invalidApiKeyException.getMessage()).trim());
            } catch (IOException e) {
                throw new AssertionError("Could not write test error response", e);
            }

            return null;
        };
    }
}

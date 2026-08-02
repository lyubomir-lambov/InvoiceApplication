package bg.softuni.invoicehistoryservice.config;

import bg.softuni.invoicehistoryservice.exception.InvalidApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final String validApiKey;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public ApiKeyAuthenticationFilter(@Value("${invoice.history.api-key}") String validApiKey,
                                      HandlerExceptionResolver handlerExceptionResolver) {
        this.validApiKey = validApiKey;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Rejected invoice history request with missing API key: method={}, path={}",
                    request.getMethod(),
                    request.getRequestURI());
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new InvalidApiKeyException(
                            "Missing API key",
                            "missing_api_key",
                            "Missing API Key",
                            HttpStatus.UNAUTHORIZED));
            return;
        }

        if (!apiKey.trim().equals(validApiKey.trim())) {
            log.warn("Rejected invoice history request with invalid API key: method={}, path={}",
                    request.getMethod(),
                    request.getRequestURI());
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new InvalidApiKeyException(
                            "Invalid API key",
                            "invalid_api_key",
                            "Invalid API Key",
                            HttpStatus.FORBIDDEN));
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthentication(apiKey.trim()));
        filterChain.doFilter(request, response);
    }
}

package bg.softuni.invoicehistoryservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final String validApiKey;

    public ApiKeyAuthenticationFilter(@Value("${invoice.history.api-key}") String validApiKey) {
        this.validApiKey = validApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing API key");
            return;
        }

        if (!apiKey.trim().equals(validApiKey.trim())) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid API key");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthentication(apiKey.trim()));
        filterChain.doFilter(request, response);
    }
}

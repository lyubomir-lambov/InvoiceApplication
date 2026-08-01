package bg.softuni.invoicehistoryservice.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final String apiKey;

    public ApiKeyAuthentication(String apiKey) {
        super(List.of(new SimpleGrantedAuthority("ROLE_INVOICE_HISTORY_CLIENT")));
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }
}

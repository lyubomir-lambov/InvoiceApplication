package bg.softuni.invoicehistoryservice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationTest {

    @Test
    void apiKeyAuthentication_shouldExposeApiKeyAndRole_whenCreated() {
        String apiKey = "lambi-invoice-history-api-key";

        ApiKeyAuthentication authentication = new ApiKeyAuthentication(apiKey);

        assertThat(authentication.getCredentials()).isEqualTo(apiKey);
        assertThat(authentication.getPrincipal()).isEqualTo(apiKey);
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_INVOICE_HISTORY_CLIENT");
    }
}

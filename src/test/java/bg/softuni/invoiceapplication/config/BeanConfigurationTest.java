package bg.softuni.invoiceapplication.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConfigurationTest {

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        BeanConfiguration beanConfiguration = new BeanConfiguration();

        PasswordEncoder passwordEncoder = beanConfiguration.passwordEncoder();

        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        assertThat(passwordEncoder.matches("password", passwordEncoder.encode("password"))).isTrue();
    }
}

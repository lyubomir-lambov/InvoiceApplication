package bg.softuni.invoiceapplication.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(matchers -> matchers
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .permitAll()
                .requestMatchers("/", "/login", "/users/register", "/users/register/success", "/error")
                .permitAll()
                .requestMatchers("/users/**")
                .hasRole("ADMIN")
                .anyRequest()
                .authenticated());

        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/invoices", true)
                .failureUrl("/login?error=true")
                .permitAll());

        http.logout(logout -> logout
                .logoutSuccessUrl("/"));


        return http.build();
    }
}

package bg.softuni.invoiceapplication.runner;

import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Profile("register-test")
public class RegisterUserTestRunner implements CommandLineRunner {

    private final UserService userService;

    public RegisterUserTestRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        String testUserSuffix = String.valueOf(System.currentTimeMillis());
        String testUsername = "test" + testUserSuffix   + testUserSuffix;

        UserRegistrationRequestDTO requestDTO = new UserRegistrationRequestDTO();
        requestDTO.setUsername(testUsername);
        requestDTO.setPassword("123456");
        requestDTO.setPasswordConfirm("123456");
        requestDTO.setEmail(testUsername + "@example.com");

        UserRegistrationResponseDTO responseDTO = userService.registerUser(requestDTO);

        System.out.println("Registered user:");
        System.out.println("Username: " + responseDTO.getUsername());
        System.out.println("Email: " + responseDTO.getEmail());
        System.out.println("Created on: " + responseDTO.getCreatedOn()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
    }
}

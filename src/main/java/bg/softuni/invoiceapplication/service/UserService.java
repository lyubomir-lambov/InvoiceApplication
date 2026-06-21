package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.*;

import java.util.UUID;

public interface UserService {
    UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO);
    UserLoginResponseDTO login(UserLoginRequestDTO userLoginRequestDTO);
    boolean isUserActive(UUID userId);

    String getUsernameById(UUID userId);

    boolean isAdmin(UUID userId);
}

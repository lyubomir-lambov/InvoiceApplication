package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.UserLoginRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserLoginResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserShowAllDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO);

    UserLoginResponseDTO login(UserLoginRequestDTO userLoginRequestDTO);

    List<UserShowAllDTO> findAllUsers();

    List<UserShowAllDTO> findUsersByUsername(String username);

    void toggleUserActive(UUID userId);

    void toggleUserRole(UUID userId);

    boolean isUserActive(UUID userId);

    String getUsernameById(UUID userId);

    boolean isAdmin(UUID userId);
}

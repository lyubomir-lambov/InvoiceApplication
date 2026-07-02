package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserShowAllDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO);

    List<UserShowAllDTO> findAllUsers();

    List<UserShowAllDTO> findUsersByUsername(String username);

    void toggleUserStatus(UUID userId);

    void toggleUserRole(UUID userId);
}

package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.users.UserProfileDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserShowAllDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO);

    List<UserShowAllDTO> findAllUsers();

    List<UserShowAllDTO> findUsersByUsername(String username);

    UserProfileDTO findUserProfile(UUID userId);

    UserProfileEditRequestDTO getUserProfileForEdit(UUID userId);

    void editUserProfile(UUID userId, UserProfileEditRequestDTO userProfileEditRequestDTO);

    void toggleUserStatus(UUID userId);

    void toggleUserRole(UUID userId);
}

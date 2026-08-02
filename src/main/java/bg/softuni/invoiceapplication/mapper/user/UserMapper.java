package bg.softuni.invoiceapplication.mapper.user;

import bg.softuni.invoiceapplication.model.dto.users.UserProfileDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public User fromUserRegistrationRequestDTOtoUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
        if (userRegistrationRequestDTO == null) {
            return null;
        }

        return User.builder()
                .username(userRegistrationRequestDTO.getUsername())
                .email(userRegistrationRequestDTO.getEmail())
                .build();
    }

    public UserRegistrationResponseDTO fromUserToUserRegistrationResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserRegistrationResponseDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .createdOn(user.getCreatedOn())
                .build();
    }

    public List<UserShowAllDTO> fromAllUsersToUserShowAllDTOs(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(user -> UserShowAllDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .active(user.isActive())
                        .createdOn(user.getCreatedOn())
                        .build())
                .toList();
    }

    public UserProfileDTO fromUserToUserProfileDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .build();
    }

    public UserProfileEditRequestDTO fromUserToUserProfileEditRequestDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileEditRequestDTO.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build();
    }

    public void updateUserFromProfileEditRequestDTO(User user,
                                                    UserProfileEditRequestDTO userProfileEditRequestDTO) {
        if (user == null || userProfileEditRequestDTO == null) {
            return;
        }

        user.setEmail(userProfileEditRequestDTO.getEmail());
        user.setFirstName(userProfileEditRequestDTO.getFirstName());
        user.setLastName(userProfileEditRequestDTO.getLastName());
        user.setPhoneNumber(userProfileEditRequestDTO.getPhoneNumber());
        user.setAddress(userProfileEditRequestDTO.getAddress());
    }
}

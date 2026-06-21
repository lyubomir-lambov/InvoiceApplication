package bg.softuni.invoiceapplication.mapper.user;

import bg.softuni.invoiceapplication.model.entity.User;
import bg.softuni.invoiceapplication.model.dto.UserLoginResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserShowAllDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public UserLoginResponseDTO fromUserToUserLoginResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserLoginResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public List<UserShowAllDTO> fromAllUsersToUserShowAllDTOs(List<User> users) {
        if (users == null) {
            return null;
        }

        List<UserShowAllDTO> userShowAllDTOs = new ArrayList<>();
        users.forEach(user -> userShowAllDTOs.add(UserShowAllDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdOn(user.getCreatedOn())
                .build()));

        return userShowAllDTOs;
    }
}

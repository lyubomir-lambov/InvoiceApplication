package bg.softuni.invoiceapplication.mapper.user;

import bg.softuni.invoiceapplication.model.User;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import org.springframework.stereotype.Component;

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
}

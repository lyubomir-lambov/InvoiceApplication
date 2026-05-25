package bg.softuni.invoiceapplication.mapper.user;

import bg.softuni.invoiceapplication.model.User;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static User fromUserRegistrationRequestDTOtoUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
        if (userRegistrationRequestDTO == null) {
            return null;
        }

        return User.builder()
                .username(userRegistrationRequestDTO.getUsername())
                .password(userRegistrationRequestDTO.getPassword())
                .email(userRegistrationRequestDTO.getEmail())
                .build();

    }
}

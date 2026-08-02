package bg.softuni.invoiceapplication.model.dto.users;

import bg.softuni.invoiceapplication.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private UUID id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String address;

    private UserRole role;
}

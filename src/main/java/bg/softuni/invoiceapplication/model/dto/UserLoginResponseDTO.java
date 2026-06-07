package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.UserRole;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponseDTO {
    private UUID id;
    private String username;
    private UserRole role;
}

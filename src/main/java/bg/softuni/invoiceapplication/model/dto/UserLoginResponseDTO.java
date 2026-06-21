package bg.softuni.invoiceapplication.model.dto;

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
public class UserLoginResponseDTO {
    private UUID id;
    private String username;
    private UserRole role;
}

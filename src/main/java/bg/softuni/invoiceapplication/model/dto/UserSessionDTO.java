package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserSessionDTO {
    private UUID id;
    private UserRole role;
    private boolean active;
}

package bg.softuni.invoiceapplication.model.dto.users;

import bg.softuni.invoiceapplication.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserShowAllDTO {

    private UUID id;

    private String username;

    private String email;

    private UserRole role;

    private boolean active;

    private LocalDateTime createdOn;
}

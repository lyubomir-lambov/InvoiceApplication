package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.UserRole;

import java.util.UUID;

public class UserSessionDTO {
    private UUID id;
    private UserRole role;
    private boolean active;
}

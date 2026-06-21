package bg.softuni.invoiceapplication.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 6, max = 20, message = "Username must be between 6 and 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Size(min = 6, max = 20, message = "Password confirmation must be between 6 and 20 characters")
    private String passwordConfirm;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
}

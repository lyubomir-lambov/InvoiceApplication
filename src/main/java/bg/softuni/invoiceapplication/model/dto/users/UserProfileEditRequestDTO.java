package bg.softuni.invoiceapplication.model.dto.users;

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
public class UserProfileEditRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @Size(max = 50, message = "First name must be up to 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be up to 50 characters")
    private String lastName;

    @Size(max = 30, message = "Phone number must be up to 30 characters")
    private String phoneNumber;

    @Size(max = 255, message = "Address must be up to 255 characters")
    private String address;
}

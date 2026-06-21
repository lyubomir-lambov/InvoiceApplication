package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.Country;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ClientCreateRequestDTO {

    @NotBlank(message = "Client name required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String displayName;

    @NotBlank(message = "Company name required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;

    @NotBlank(message = "Legal representative required")
    @Size(min = 2, max = 255, message = "Legal representative must be between 2 and 255 characters")
    private String legalRepresentative;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Country required")
    private Country country;

    private String address;

    @NotNull(message = "VAT registered selection required")
    private Boolean vatRegistered;

    @Size(max = 32, message = "VAT number must be up to 32 characters")
    private String vatNumber;

}

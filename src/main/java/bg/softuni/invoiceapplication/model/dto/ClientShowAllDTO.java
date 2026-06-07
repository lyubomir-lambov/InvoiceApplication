package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.Country;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ClientShowAllDTO {

    private UUID id;

    private String displayName;

    private String companyName;

    private String email;

    private String phoneNumber;

    private Country country;

    private String address;

    private boolean vatRegistered;

    private String vatNumber;

    private boolean active;

}

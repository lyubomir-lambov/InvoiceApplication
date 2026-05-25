package bg.softuni.invoiceapplication.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationResponseDTO {
    private String username;
    private String email;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss") //! Искам ли месеца да е име
    private LocalDateTime createdOn;
}

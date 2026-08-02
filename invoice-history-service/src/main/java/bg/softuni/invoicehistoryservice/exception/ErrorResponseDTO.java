package bg.softuni.invoicehistoryservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    private int status;

    private String errorCode;

    private String errorTitle;

    private String message;

    private String path;

    private LocalDateTime timestamp;
}

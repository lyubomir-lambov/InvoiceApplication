package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PaymentEditRequestDTO {

    @NotNull(message = "Payment id required")
    private UUID id;

    @NotNull(message = "Client required")
    private UUID clientId;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Currency required")
    private InvoiceCurrency currency;

    @NotNull(message = "Payment date required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    @Size(max = 1000, message = "Notes must be up to 1000 characters")
    private String notes;
}

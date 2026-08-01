package bg.softuni.invoicehistoryservice.model.dto;

import bg.softuni.invoicehistoryservice.model.enums.InvoiceHistoryAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceHistoryCreateRequestDTO {

    @NotNull(message = "Invoice id required")
    private UUID invoiceId;

    @NotBlank(message = "Invoice number required")
    @Size(min = 10, max = 10, message = "Invoice number must be exactly 10 characters")
    @Pattern(regexp = "\\d{10}", message = "Invoice number must contain exactly 10 digits")
    private String invoiceNumber;

    @NotBlank(message = "Invoice type required")
    private String invoiceType;

    @NotBlank(message = "Invoice status required")
    private String invoiceStatus;

    @NotNull(message = "Invoice history action required")
    private InvoiceHistoryAction action;

    @NotBlank(message = "Currency required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    @NotBlank(message = "Client display name required")
    private String clientDisplayName;

    @NotBlank(message = "Client company name required")
    private String clientCompanyName;

    @NotNull(message = "Issue date required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @NotNull(message = "Total without VAT required")
    @PositiveOrZero(message = "Total without VAT must be zero or positive")
    private BigDecimal totalWithoutVat;

    @NotNull(message = "Total VAT required")
    @PositiveOrZero(message = "Total VAT must be zero or positive")
    private BigDecimal totalVat;

    @NotNull(message = "Total with VAT required")
    @PositiveOrZero(message = "Total with VAT must be zero or positive")
    private BigDecimal totalWithVat;

    @NotBlank(message = "Invoice snapshot required")
    private String snapshotJson;

    @NotBlank(message = "Performed by username required")
    private String performedByUsername;
}

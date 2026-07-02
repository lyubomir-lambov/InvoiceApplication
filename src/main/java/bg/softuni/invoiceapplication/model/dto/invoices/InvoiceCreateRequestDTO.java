package bg.softuni.invoiceapplication.model.dto.invoices;

import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCreateRequestDTO {

    @NotNull(message = "Invoice type required")
    private InvoiceType invoiceType;

    @NotNull(message = "Invoice sequence required")
    @Positive(message = "Invoice sequence must be positive")
    private Long invoiceSequence;

    @NotBlank(message = "Invoice number required")
    @Size(min = 10, max = 10, message = "Invoice number must be exactly 10 characters")
    @Pattern(regexp = "\\d{10}", message = "Invoice number must contain exactly 10 digits")
    private String invoiceNumber;

    @NotNull(message = "Invoice currency required")
    private InvoiceCurrency currency;

    @NotNull(message = "Issue date required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;

    @NotNull(message = "Due date required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @NotNull(message = "Client required")
    private UUID clientId;

    @Valid
    @NotEmpty(message = "At least one invoice line item is required")
    @Builder.Default
    private List<InvoiceLineItemCreateRequestDTO> lineItems = new ArrayList<>();
}

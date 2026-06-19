package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class InvoiceCreateRequestDTO {

    @NotNull(message = "Invoice type required")
    private InvoiceType invoiceType;

    @NotNull(message = "Issue date required")
    private LocalDate issueDate;

    @NotNull(message = "Due date required")
    private LocalDate dueDate;

    @NotNull(message = "Client required")
    private UUID clientId;
}

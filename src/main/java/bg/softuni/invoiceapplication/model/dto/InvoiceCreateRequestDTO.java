package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class InvoiceCreateRequestDTO {

    private InvoiceType invoiceType;

    private String invoiceNumber;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private UUID clientId;
}

package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class InvoiceShowAllDTO {

    private UUID id;

    private InvoiceType invoiceType;

    private String invoiceNumber;

    private String clientCompanyName;

    private LocalDate issueDate;

    private BigDecimal totalAmount;
}

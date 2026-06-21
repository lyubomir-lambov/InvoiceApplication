package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportInvoiceDTO {

    private UUID id;

    private String invoiceNumber;

    private InvoiceType invoiceType;

    private LocalDate issueDate;

    private BigDecimal totalAmount;

    private InvoiceCurrency currency;
}

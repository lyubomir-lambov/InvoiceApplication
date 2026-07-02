package bg.softuni.invoiceapplication.model.dto.reports;

import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
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
public class PaymentReportPaymentDTO {

    private UUID id;

    private LocalDate paymentDate;

    private BigDecimal amount;

    private InvoiceCurrency currency;

    private String notes;
}

package bg.softuni.invoiceapplication.model.dto.reports;

import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportByCurrencyDTO {

    private InvoiceCurrency currency;

    private BigDecimal invoiceTotal;

    private BigDecimal paymentTotal;

    private BigDecimal dueAmount;
}

package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportPaymentCurrencyGroupDTO {

    private InvoiceCurrency currency;

    private BigDecimal totalAmount;

    @Builder.Default
    private List<PaymentReportPaymentDTO> payments = new ArrayList<>();
}

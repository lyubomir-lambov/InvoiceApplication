package bg.softuni.invoiceapplication.model.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportClientDTO {

    private UUID clientId;

    private String clientDisplayName;

    private String clientCompanyName;

    @Builder.Default
    private List<PaymentReportInvoiceCurrencyGroupDTO> invoiceCurrencyGroups = new ArrayList<>();

    @Builder.Default
    private List<PaymentReportPaymentCurrencyGroupDTO> paymentCurrencyGroups = new ArrayList<>();
}

package bg.softuni.invoiceapplication.model.dto.invoicehistory.snapshot;

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
public class InvoiceHistoryLineItemSnapshotDTO {

    private String description;

    private BigDecimal quantity;

    private String measurementUnit;

    private BigDecimal unitPrice;

    private String vatRate;

    private BigDecimal lineTotalWithoutVat;

    private BigDecimal vatAmount;

    private BigDecimal lineTotalWithVat;
}

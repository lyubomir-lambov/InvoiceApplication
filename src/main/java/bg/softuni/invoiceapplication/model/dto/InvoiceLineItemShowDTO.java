package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class InvoiceLineItemShowDTO {

    private String description;

    private BigDecimal quantity;

    private MeasurementUnit measurementUnit;

    private BigDecimal unitPrice;

    private VatRate vatRate;

    private BigDecimal lineTotalWithoutVat;

    private BigDecimal vatAmount;

    private BigDecimal lineTotalWithVat;
}

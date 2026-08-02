package bg.softuni.invoiceapplication.model.entity;

import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceLineItemTest {

    @Test
    void getLineTotalWithoutVat_shouldReturnQuantityMultipliedByUnitPrice() {
        InvoiceLineItem lineItem = createLineItem("7.00", "2.00", VatRate.TWENTY);

        BigDecimal result = lineItem.getLineTotalWithoutVat();

        assertThat(result).isEqualByComparingTo("14.00");
    }

    @Test
    void getVatAmount_shouldReturnCalculatedVatAmount() {
        InvoiceLineItem lineItem = createLineItem("7.00", "2.00", VatRate.TWENTY);

        BigDecimal result = lineItem.getVatAmount();

        assertThat(result).isEqualByComparingTo("2.80");
    }

    @Test
    void getLineTotalWithVat_shouldReturnTotalIncludingVat() {
        InvoiceLineItem lineItem = createLineItem("7.00", "2.00", VatRate.TWENTY);

        BigDecimal result = lineItem.getLineTotalWithVat();

        assertThat(result).isEqualByComparingTo("16.80");
    }

    @Test
    void getVatAmount_shouldReturnZero_whenVatRateIsZero() {
        InvoiceLineItem lineItem = createLineItem("7.00", "2.00", VatRate.ZERO);

        BigDecimal result = lineItem.getVatAmount();

        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void getLineTotalWithoutVat_shouldRoundHalfUpToTwoDecimals() {
        InvoiceLineItem lineItem = createLineItem("3.00", "1.335", VatRate.TWENTY);

        BigDecimal result = lineItem.getLineTotalWithoutVat();

        assertThat(result).isEqualByComparingTo("4.01");
    }

    private InvoiceLineItem createLineItem(String quantity, String unitPrice, VatRate vatRate) {
        return InvoiceLineItem.builder()
                .description("Test service")
                .quantity(new BigDecimal(quantity))
                .measurementUnit(MeasurementUnit.SERVICE)
                .unitPrice(new BigDecimal(unitPrice))
                .vatRate(vatRate)
                .build();
    }
}

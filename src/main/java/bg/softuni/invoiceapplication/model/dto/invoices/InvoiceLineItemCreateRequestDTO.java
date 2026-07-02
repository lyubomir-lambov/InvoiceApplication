package bg.softuni.invoiceapplication.model.dto.invoices;

import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class InvoiceLineItemCreateRequestDTO {

    @NotBlank(message = "Description required")
    @Size(max = 255, message = "Description must be up to 255 characters")
    private String description;

    @NotNull(message = "Quantity required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Measurement unit required")
    private MeasurementUnit measurementUnit;

    @NotNull(message = "Unit price required")
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @NotNull(message = "VAT rate required")
    private VatRate vatRate;
}

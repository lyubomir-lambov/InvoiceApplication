package bg.softuni.invoiceapplication.model.enums;

import lombok.Getter;

@Getter
public enum VatRate {
    ZERO(0),
    NINE(9),
    TWENTY(20);

    private final int percentage;

    VatRate(int percentage) {
        this.percentage = percentage;
    }
}

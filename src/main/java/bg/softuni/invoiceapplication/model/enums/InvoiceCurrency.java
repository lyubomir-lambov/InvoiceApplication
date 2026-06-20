package bg.softuni.invoiceapplication.model.enums;

import lombok.Getter;

@Getter
public enum InvoiceCurrency {
    BGN("Bulgarian lev", "BGN"),
    EUR("Euro", "EUR"),
    USD("US dollar", "USD"),
    GBP("British pound", "GBP");

    private final String displayName;
    private final String code;

    InvoiceCurrency(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
}

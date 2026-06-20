package bg.softuni.invoiceapplication.model.enums;

public enum InvoiceStatus {
    ISSUED("Issued"),
    CANCELLED("Cancelled");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

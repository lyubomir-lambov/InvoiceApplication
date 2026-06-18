package bg.softuni.invoiceapplication.model.enums;

public enum InvoiceType {
    INVOICE("Invoice"),
    DEBIT_NOTE("Debit note"),
    CREDIT_NOTE("Credit note");

    private final String displayName;

    InvoiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

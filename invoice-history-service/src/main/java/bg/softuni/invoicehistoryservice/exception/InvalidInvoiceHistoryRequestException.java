package bg.softuni.invoicehistoryservice.exception;

public class InvalidInvoiceHistoryRequestException extends ApplicationException {

    public InvalidInvoiceHistoryRequestException(String message) {
        super(message, "invalid_invoice_history_request", "Invalid Invoice History Request");
    }
}

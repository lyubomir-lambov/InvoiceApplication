package bg.softuni.invoicehistoryservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidApiKeyException extends ApplicationException {

    public InvalidApiKeyException(String message, String errorCode, String errorTitle, HttpStatus httpStatus) {
        super(message, errorCode, errorTitle, httpStatus);
    }
}

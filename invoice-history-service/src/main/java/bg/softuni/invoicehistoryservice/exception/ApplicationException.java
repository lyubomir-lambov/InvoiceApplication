package bg.softuni.invoicehistoryservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final String errorTitle;
    private final HttpStatus httpStatus;

    public ApplicationException(String message, String errorCode, String errorTitle) {
        this(message, errorCode, errorTitle, HttpStatus.BAD_REQUEST);
    }

    public ApplicationException(String message, String errorCode, String errorTitle, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.errorTitle = errorTitle;
        this.httpStatus = httpStatus;
    }
}

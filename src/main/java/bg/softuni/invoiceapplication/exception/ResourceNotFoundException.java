package bg.softuni.invoiceapplication.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String message) {
        super(message, "resource_not_found", "Resource Not Found", HttpStatus.NOT_FOUND);
    }
}

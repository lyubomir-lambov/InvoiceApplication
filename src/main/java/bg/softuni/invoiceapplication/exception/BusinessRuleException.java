package bg.softuni.invoiceapplication.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApplicationException {

    public BusinessRuleException(String message) {
        super(message, "business_rule_violation", "Business Rule Violation", HttpStatus.BAD_REQUEST);
    }
}

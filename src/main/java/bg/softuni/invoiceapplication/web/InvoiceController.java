package bg.softuni.invoiceapplication.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InvoiceController {

    @GetMapping("/invoices")
    public String invoices() {
        return "invoices";
    }
}

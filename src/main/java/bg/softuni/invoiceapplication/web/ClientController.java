package bg.softuni.invoiceapplication.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    @GetMapping("/clients")
    public String clients() {
        return "clients";
    }
}

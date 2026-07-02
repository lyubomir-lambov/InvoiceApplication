package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal AuthenticatedUserDetails currentUser) {
        if (currentUser != null) {
            return "redirect:/invoices";
        }

        return "index";
    }
}

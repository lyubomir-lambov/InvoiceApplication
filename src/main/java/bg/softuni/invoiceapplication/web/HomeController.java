package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(HttpSession httpSession) {
        if (SessionUser.getUserId(httpSession) != null) {
            return "redirect:/invoices";
        }

        return "index";
    }
}

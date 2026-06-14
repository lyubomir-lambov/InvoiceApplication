package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class InvoiceController {

    private final UserService userService;

    public InvoiceController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/invoices")
    public String invoices(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);
        model.addAttribute("username", username);
        return "invoices";
    }
}

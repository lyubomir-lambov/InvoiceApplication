package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.PaymentReportService;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class PaymentReportController {

    private final UserService userService;
    private final PaymentReportService paymentReportService;

    public PaymentReportController(UserService userService, PaymentReportService paymentReportService) {
        this.userService = userService;
        this.paymentReportService = paymentReportService;
    }

    @GetMapping("/payment-reports")
    public String showPaymentReports(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("reports", paymentReportService.getReportsByCurrency());

        return "payment-reports";
    }
}

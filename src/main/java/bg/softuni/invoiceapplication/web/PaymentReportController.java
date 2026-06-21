package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.PaymentReportService;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class PaymentReportController {

    private final UserService userService;
    private final PaymentReportService paymentReportService;
    private final ClientService clientService;

    public PaymentReportController(UserService userService,
                                   PaymentReportService paymentReportService,
                                   ClientService clientService) {
        this.userService = userService;
        this.paymentReportService = paymentReportService;
        this.clientService = clientService;
    }

    @GetMapping("/payment-reports")
    public String showPaymentReports(@RequestParam(required = false) UUID clientId,
                                     HttpSession httpSession,
                                     Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
        model.addAttribute("selectedClientId", clientId);
        model.addAttribute("reports", paymentReportService.getReportsByCurrency(clientId));
        model.addAttribute("clientReports", paymentReportService.getClientReports(clientId));

        return "payment-reports";
    }
}

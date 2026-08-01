package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.PaymentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PaymentReportController {

    private final PaymentReportService paymentReportService;
    private final ClientService clientService;

    @GetMapping("/payment-reports")
    public String showPaymentReports(@RequestParam(required = false) UUID clientId,
                                     Model model) {
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
        model.addAttribute("selectedClientId", clientId);
        model.addAttribute("reports", paymentReportService.getReportsByCurrency(clientId));
        model.addAttribute("clientReports", paymentReportService.getClientReports(clientId));

        return "payment-reports";
    }
}

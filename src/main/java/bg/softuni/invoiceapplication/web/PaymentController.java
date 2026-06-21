package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.PaymentService;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final ClientService clientService;

    public PaymentController(PaymentService paymentService, UserService userService, ClientService clientService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.clientService = clientService;
    }

    @GetMapping("")
    public String showAllPayments(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("payments", paymentService.findAllPayments());

        return "payments";
    }

    @GetMapping("/create")
    public String showCreatePaymentForm(HttpSession httpSession, Model model) {
        addCreatePaymentFormAttributes(httpSession, model);
        model.addAttribute("payment", new PaymentCreateRequestDTO());

        return "payment-create";
    }

    @PostMapping("/create")
    public String createPayment(@Valid @ModelAttribute("payment") PaymentCreateRequestDTO paymentCreateRequestDTO,
                                BindingResult bindingResult,
                                HttpSession httpSession,
                                Model model) {
        if (bindingResult.hasErrors()) {
            addCreatePaymentFormAttributes(httpSession, model);
            return "payment-create";
        }

        paymentService.createPayment(paymentCreateRequestDTO);
        return "redirect:/payments";
    }

    private void addCreatePaymentFormAttributes(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
    }
}

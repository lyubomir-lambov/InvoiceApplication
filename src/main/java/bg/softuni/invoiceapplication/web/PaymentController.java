package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.PaymentEditRequestDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showAllPayments(@RequestParam(required = false) String companyName,
                                  HttpSession httpSession,
                                  Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("payments", paymentService.findPaymentsByCompanyName(companyName));
        model.addAttribute("companyName", companyName);

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

    @GetMapping("/edit/{paymentId}")
    public String showEditPaymentForm(@PathVariable UUID paymentId,
                                      HttpSession httpSession,
                                      Model model) {
        PaymentEditRequestDTO paymentEditRequestDTO = paymentService.getPaymentForEdit(paymentId);
        addEditPaymentFormAttributes(httpSession, model, paymentEditRequestDTO.getClientId());
        model.addAttribute("payment", paymentEditRequestDTO);

        return "payment-edit";
    }

    @PostMapping("/edit/{paymentId}")
    public String editPayment(@PathVariable UUID paymentId,
                              @Valid @ModelAttribute("payment") PaymentEditRequestDTO paymentEditRequestDTO,
                              BindingResult bindingResult,
                              HttpSession httpSession,
                              Model model) {
        if (bindingResult.hasErrors()) {
            addEditPaymentFormAttributes(httpSession, model, paymentEditRequestDTO.getClientId());
            return "payment-edit";
        }

        paymentEditRequestDTO.setId(paymentId);
        paymentService.editPayment(paymentEditRequestDTO);
        return "redirect:/payments";
    }

    @PostMapping("/{paymentId}/delete")
    public String deletePayment(@PathVariable UUID paymentId, RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePayment(paymentId);
            redirectAttributes.addFlashAttribute("message", "Payment deleted successfully");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/payments";
    }

    private void addCreatePaymentFormAttributes(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
    }

    private void addEditPaymentFormAttributes(HttpSession httpSession, Model model, UUID selectedClientId) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect(selectedClientId));
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
    }
}

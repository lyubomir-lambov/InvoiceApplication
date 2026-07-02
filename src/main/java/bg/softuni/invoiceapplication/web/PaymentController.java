package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.payments.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ClientService clientService;

    public PaymentController(PaymentService paymentService, ClientService clientService) {
        this.paymentService = paymentService;
        this.clientService = clientService;
    }

    @GetMapping("")
    public String showAllPayments(@RequestParam(required = false) String companyName, Model model) {
        model.addAttribute("payments", paymentService.findPaymentsByCompanyName(companyName));
        model.addAttribute("companyName", companyName);

        return "payments";
    }

    @GetMapping("/create")
    public String showCreatePaymentForm(Model model) {
        addCreatePaymentFormAttributes(model);
        model.addAttribute("payment", new PaymentCreateRequestDTO());

        return "payment-create";
    }

    @PostMapping("/create")
    public String createPayment(@Valid @ModelAttribute("payment") PaymentCreateRequestDTO paymentCreateRequestDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            addCreatePaymentFormAttributes(model);
            return "payment-create";
        }

        paymentService.createPayment(paymentCreateRequestDTO);
        return "redirect:/payments";
    }

    @GetMapping("/edit/{paymentId}")
    public String showEditPaymentForm(@PathVariable UUID paymentId, Model model) {
        PaymentEditRequestDTO paymentEditRequestDTO = paymentService.getPaymentForEdit(paymentId);
        addEditPaymentFormAttributes(model, paymentEditRequestDTO.getClientId());
        model.addAttribute("payment", paymentEditRequestDTO);

        return "payment-edit";
    }

    @PostMapping("/edit/{paymentId}")
    public String editPayment(@PathVariable UUID paymentId, @Valid @ModelAttribute("payment") PaymentEditRequestDTO paymentEditRequestDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            addEditPaymentFormAttributes(model, paymentEditRequestDTO.getClientId());
            return "payment-edit";
        }

        paymentEditRequestDTO.setId(paymentId);
        paymentService.editPayment(paymentEditRequestDTO);
        return "redirect:/payments";
    }

    @PostMapping("/{paymentId}/delete")
    public String deletePayment(@PathVariable UUID paymentId,
                                @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        if (currentUser == null || !UserRole.ADMIN.equals(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("message", "Only admins can delete payments");
            return "redirect:/payments";
        }

        try {
            paymentService.deletePayment(paymentId);
            redirectAttributes.addFlashAttribute("message", "Payment deleted successfully");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/payments";
    }

    private void addCreatePaymentFormAttributes(Model model) {
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
    }

    private void addEditPaymentFormAttributes(Model model, UUID selectedClientId) {
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect(selectedClientId));
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
    }
}

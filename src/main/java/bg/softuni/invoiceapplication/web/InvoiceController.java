package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ClientService clientService;

    @GetMapping("")
    public String invoices(@RequestParam(required = false) String companyName, Model model) {
        model.addAttribute("invoices", invoiceService.findInvoicesByCompanyName(companyName));
        model.addAttribute("companyName", companyName);
        return "invoices";
    }

    @GetMapping("/create")
    public String showCreateInvoiceForm(Model model) {
        addCreateInvoiceFormAttributes(model);
        model.addAttribute("invoice", invoiceService.prepareCreateInvoiceForm());

        return "invoice-create";
    }

    @GetMapping("/{invoiceId}")
    public String showInvoice(@PathVariable UUID invoiceId, Model model) {
        model.addAttribute("invoice", invoiceService.findInvoiceById(invoiceId));

        return "invoice-details";
    }

    @PostMapping("/create")
    public String createInvoice(@Valid @ModelAttribute("invoice") InvoiceCreateRequestDTO invoiceCreateRequestDTO,
                                BindingResult bindingResult,
                                Model model,
                                @AuthenticationPrincipal AuthenticatedUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            addCreateInvoiceFormAttributes(model);
            return "invoice-create";
        }

        invoiceService.createInvoice(invoiceCreateRequestDTO, getCurrentUsername(currentUser));
        return "redirect:/invoices";
    }

    @GetMapping("/edit/{invoiceId}")
    public String showEditInvoiceForm(@PathVariable UUID invoiceId,
                                      Model model) {
        InvoiceEditRequestDTO invoiceEditRequestDTO = invoiceService.getInvoiceForEdit(invoiceId);
        addEditInvoiceFormAttributes(model, invoiceEditRequestDTO.getClientId());
        model.addAttribute("invoice", invoiceEditRequestDTO);

        return "invoice-edit";
    }

    @PostMapping("/edit/{invoiceId}")
    public String editInvoice(@PathVariable UUID invoiceId,
                              @Valid @ModelAttribute("invoice") InvoiceEditRequestDTO invoiceEditRequestDTO,
                              BindingResult bindingResult,
                              Model model,
                              @AuthenticationPrincipal AuthenticatedUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            addEditInvoiceFormAttributes(model, invoiceEditRequestDTO.getClientId());
            return "invoice-edit";
        }

        invoiceEditRequestDTO.setId(invoiceId);
        invoiceService.editInvoice(invoiceEditRequestDTO, getCurrentUsername(currentUser));
        return "redirect:/invoices";
    }

    @PostMapping("/edit/{invoiceId}/cancel")
    public String cancelInvoice(@PathVariable UUID invoiceId,
                                @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        if (!isCurrentUserAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("message", "Only admins can cancel invoices");
            return "redirect:/invoices/edit/" + invoiceId;
        }

        invoiceService.cancelInvoice(invoiceId, getCurrentUsername(currentUser));
        return "redirect:/invoices";
    }

    @PostMapping("/edit/{invoiceId}/restore")
    public String restoreInvoice(@PathVariable UUID invoiceId,
                                 @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                                 RedirectAttributes redirectAttributes) {
        if (!isCurrentUserAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("message", "Only admins can restore invoices");
            return "redirect:/invoices/edit/" + invoiceId;
        }

        invoiceService.restoreInvoice(invoiceId, getCurrentUsername(currentUser));
        return "redirect:/invoices";
    }

    private void addCreateInvoiceFormAttributes(Model model) {
        model.addAttribute("invoiceTypes", InvoiceType.values());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
        model.addAttribute("measurementUnits", MeasurementUnit.values());
        model.addAttribute("vatRates", VatRate.values());
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
    }

    private void addEditInvoiceFormAttributes(Model model, UUID selectedClientId) {
        model.addAttribute("invoiceTypes", InvoiceType.values());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
        model.addAttribute("measurementUnits", MeasurementUnit.values());
        model.addAttribute("vatRates", VatRate.values());
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect(selectedClientId));
    }

    private boolean isCurrentUserAdmin(AuthenticatedUserDetails currentUser) {
        return currentUser != null && UserRole.ADMIN.equals(currentUser.getRole());
    }

    private String getCurrentUsername(AuthenticatedUserDetails currentUser) {
        return currentUser == null ? null : currentUser.getUsername();
    }
}

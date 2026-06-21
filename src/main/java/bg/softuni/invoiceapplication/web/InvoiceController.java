package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.InvoiceService;
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

import java.util.UUID;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final ClientService clientService;

    public InvoiceController(UserService userService, InvoiceService invoiceService, ClientService clientService) {
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.clientService = clientService;
    }

    @GetMapping("")
    public String invoices(@RequestParam(required = false) String companyName, HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);
        model.addAttribute("username", username);
        model.addAttribute("invoices", invoiceService.findInvoicesByCompanyName(companyName));
        model.addAttribute("companyName", companyName);
        return "invoices";
    }

    @GetMapping("/create")
    public String showCreateInvoiceForm(HttpSession httpSession, Model model) {
        addCreateInvoiceFormAttributes(httpSession, model);
        model.addAttribute("invoice", invoiceService.prepareCreateInvoiceForm());

        return "invoice-create";
    }

    @GetMapping("/{invoiceId}")
    public String showInvoice(@PathVariable UUID invoiceId,
                              HttpSession httpSession,
                              Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("invoice", invoiceService.findInvoiceById(invoiceId));

        return "invoice-details";
    }

    @PostMapping("/create")
    public String createInvoice(@Valid @ModelAttribute("invoice") InvoiceCreateRequestDTO invoiceCreateRequestDTO,
                                BindingResult bindingResult,
                                HttpSession httpSession,
                                Model model) {
        if (bindingResult.hasErrors()) {
            addCreateInvoiceFormAttributes(httpSession, model);
            return "invoice-create";
        }

        invoiceService.createInvoice(invoiceCreateRequestDTO);
        return "redirect:/invoices";
    }

    @GetMapping("/edit/{invoiceId}")
    public String showEditInvoiceForm(@PathVariable UUID invoiceId,
                                      HttpSession httpSession,
                                      Model model) {
        InvoiceEditRequestDTO invoiceEditRequestDTO = invoiceService.getInvoiceForEdit(invoiceId);
        addEditInvoiceFormAttributes(httpSession, model, invoiceEditRequestDTO.getClientId());
        model.addAttribute("invoice", invoiceEditRequestDTO);

        return "invoice-edit";
    }

    @PostMapping("/edit/{invoiceId}")
    public String editInvoice(@PathVariable UUID invoiceId,
                              @Valid @ModelAttribute("invoice") InvoiceEditRequestDTO invoiceEditRequestDTO,
                              BindingResult bindingResult,
                              HttpSession httpSession,
                              Model model) {
        if (bindingResult.hasErrors()) {
            addEditInvoiceFormAttributes(httpSession, model, invoiceEditRequestDTO.getClientId());
            return "invoice-edit";
        }

        invoiceEditRequestDTO.setId(invoiceId);
        invoiceService.editInvoice(invoiceEditRequestDTO);
        return "redirect:/invoices";
    }

    @PostMapping("/edit/{invoiceId}/cancel")
    public String cancelInvoice(@PathVariable UUID invoiceId) {
        invoiceService.cancelInvoice(invoiceId);
        return "redirect:/invoices";
    }

    @PostMapping("/edit/{invoiceId}/restore")
    public String restoreInvoice(@PathVariable UUID invoiceId) {
        invoiceService.restoreInvoice(invoiceId);
        return "redirect:/invoices";
    }

    private void addCreateInvoiceFormAttributes(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("invoiceTypes", InvoiceType.values());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
        model.addAttribute("measurementUnits", MeasurementUnit.values());
        model.addAttribute("vatRates", VatRate.values());
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
    }

    private void addEditInvoiceFormAttributes(HttpSession httpSession, Model model, UUID selectedClientId) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("invoiceTypes", InvoiceType.values());
        model.addAttribute("invoiceCurrencies", InvoiceCurrency.values());
        model.addAttribute("measurementUnits", MeasurementUnit.values());
        model.addAttribute("vatRates", VatRate.values());
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect(selectedClientId));
    }
}

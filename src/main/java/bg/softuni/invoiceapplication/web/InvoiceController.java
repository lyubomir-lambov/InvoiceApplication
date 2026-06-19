package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String invoices(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);
        model.addAttribute("username", username);
        return "invoices";
    }

    @GetMapping("/create")
    public String showCreateInvoiceForm(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("invoice", invoiceService.prepareCreateInvoiceForm());
        model.addAttribute("invoiceTypes", InvoiceType.values());
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());

        return "invoice-create";
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

    private void addCreateInvoiceFormAttributes(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("username", username);
        model.addAttribute("invoiceTypes", InvoiceType.values());
        model.addAttribute("clients", clientService.findAllActiveClientsForSelect());
    }
}

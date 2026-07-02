package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
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

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("")
    public String clientsShowAll(@RequestParam(required = false) String clientName, Model model) {
        model.addAttribute("clients", clientService.findClientsByName(clientName));
        model.addAttribute("clientName", clientName);
        return "clients";
    }

    @GetMapping("/create")
    public String showCreateClientForm(Model model) {
        model.addAttribute("client", new ClientCreateRequestDTO());
        model.addAttribute("countries", Country.values());
        return "client-create";
    }

    @PostMapping("/create")
    public String createClient(@Valid @ModelAttribute("client") ClientCreateRequestDTO clientCreateRequestDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("countries", Country.values());
            return "client-create";
        }
        clientService.createClient(clientCreateRequestDTO);
        return "redirect:/clients";
    }

    @GetMapping("/edit/{id}")
    public String editClient(@PathVariable UUID id, Model model) {
        ClientEditRequestDTO clientEditRequestDTO = clientService.getClientForEdit(id);

        model.addAttribute("client", clientEditRequestDTO);
        model.addAttribute("countries", Country.values());

        return "client-edit";
    }

    @PostMapping("/edit/{id}")
    public String editClient(@PathVariable UUID id, @Valid @ModelAttribute("client") ClientEditRequestDTO clientEditRequestDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("countries", Country.values());
            return "client-edit";
        }
        clientEditRequestDTO.setId(id);
        Optional<String> duplicateField = clientService.findDuplicateFieldForEdit(clientEditRequestDTO);
        if (duplicateField.isPresent()) {
            String errorMessage = duplicateField.get().equals("displayName")
                    ? "Client with this display name already exists"
                    : "Client with this VAT number already exists";

            bindingResult.rejectValue(
                    duplicateField.get(),
                    "client.duplicate",
                    errorMessage
            );
            model.addAttribute("countries", Country.values());
            return "client-edit";
        }

        clientService.editClient(clientEditRequestDTO);
        return "redirect:/clients";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleClientStatus(@PathVariable UUID id) {
        clientService.toggleClientActive(id);
        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable UUID id,
                               @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                               RedirectAttributes redirectAttributes) {
        if (currentUser == null || !UserRole.ADMIN.equals(currentUser.getRole())) {
            return redirectToClientsWithMessage(redirectAttributes, "Only admins can delete clients");
        }

        try {
            clientService.deleteClient(id);
            return redirectToClientsWithMessage(redirectAttributes, "Client deleted successfully");
        } catch (IllegalStateException ex) {
            return redirectToClientsWithMessage(redirectAttributes, ex.getMessage());
        }
    }

    private String redirectToClientsWithMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/clients";
    }
}

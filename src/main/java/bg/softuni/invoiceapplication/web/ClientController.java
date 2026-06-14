package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;

    @Autowired
    public ClientController(ClientService clientService, UserService userService) {
        this.clientService = clientService;
        this.userService = userService;
    }

    @GetMapping("")
    public String clientsShowAll(HttpSession httpSession, Model model) {
        UUID userId = SessionUser.getUserId(httpSession);
        String username = userService.getUsernameById(userId);

        model.addAttribute("clients", clientService.findAllClients());
        model.addAttribute("username", username);
        return "clients";
    }

    @GetMapping("/create")
    public String showCreateClientForm(Model model) {
        model.addAttribute("client", new ClientCreateRequestDTO());
        model.addAttribute("countries", Country.values());
        return "client-create";
    }

    @PostMapping("/create")
    public String createClient(@Valid @ModelAttribute("client")
                               ClientCreateRequestDTO clientCreateRequestDTO,
                               BindingResult bindingResult,
                               Model model) {
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
    public String editClient(@PathVariable UUID id,
                             @Valid @ModelAttribute("client")
                             ClientEditRequestDTO clientEditRequestDTO,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("countries", Country.values());
            return "client-edit";
        }
        clientEditRequestDTO.setId(id);
        clientService.editClient(clientEditRequestDTO);
        return "redirect:/clients";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleClientStatus(@PathVariable UUID id) {
        clientService.toggleClientActive(id);
        return "redirect:/clients";
    }
}

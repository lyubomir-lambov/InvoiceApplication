package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("")
    public String clientsShowAll(Model model) {
        model.addAttribute("clients", clientService.findAllClients());
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
}

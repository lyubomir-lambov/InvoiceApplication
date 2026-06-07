package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.enums.Country;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @GetMapping("")
    public String clients() {
        return "clients";
    }

    @GetMapping("create")
    public String createClient(Model model) {
        model.addAttribute("client", new ClientCreateRequestDTO());
        model.addAttribute("countries", Country.values());
        return "client-create";
    }
}

package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.enums.Country;
import jakarta.validation.Valid;
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

    @GetMapping("")
    public String clients() {
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
        //! сървис регистрация
        return "redirect:/clients";
    }
}

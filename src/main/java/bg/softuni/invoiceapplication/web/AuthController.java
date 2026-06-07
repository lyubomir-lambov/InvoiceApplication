package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.UserLoginRequestDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new UserLoginRequestDTO());
        return "user-login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("user") UserLoginRequestDTO user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "user-login";
        }
        return "redirect:/invoices";
    }

}

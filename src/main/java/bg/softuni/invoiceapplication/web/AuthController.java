package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.UserLoginRequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new UserLoginRequestDTO());
        return "user-login";
    }

}

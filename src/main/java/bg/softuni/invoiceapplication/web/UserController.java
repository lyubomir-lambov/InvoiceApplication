package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    @GetMapping("/register")
    public String registerUser(Model model) {
        model.addAttribute("user", new UserRegistrationRequestDTO());
        return "user-register";
    }
}

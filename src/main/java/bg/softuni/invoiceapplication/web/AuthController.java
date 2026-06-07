package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.UserLoginRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserLoginResponseDTO;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

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
        //! Тук след време ще трябва да върна сесия
        //! За момента само минаваме през проверките за Login и пренасочваме към invoices
//        userService.login(user);
//        UserLoginResponseDTO userLoginResponseDTO = userService.login(user);
//        model.addAttribute("user", userLoginResponseDTO);

        UserLoginResponseDTO userLoginResponseDTO = userService.login(user);

        System.out.println("Logged user id: " + userLoginResponseDTO.getId());
        System.out.println("Logged username: " + userLoginResponseDTO.getUsername());
        System.out.println("Logged user role: " + userLoginResponseDTO.getRole());

        return "redirect:/invoices";
    }

}

package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.UserLoginRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserLoginResponseDTO;
import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpSession;
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
    public String login(@Valid @ModelAttribute("user") UserLoginRequestDTO userLoginRequestDTO, BindingResult bindingResult, HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            return "user-login";
        }

        UserLoginResponseDTO userLoginResponseDTO = userService.login(userLoginRequestDTO);
        SessionUser.setUserId(httpSession, userLoginResponseDTO.getId());

        return "redirect:/invoices";
    }

    @PostMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.invalidate();
        return "redirect:/";
    }
}

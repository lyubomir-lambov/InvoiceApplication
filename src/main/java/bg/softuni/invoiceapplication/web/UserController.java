package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.UserService;
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

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String users(@RequestParam(required = false) String username, @AuthenticationPrincipal AuthenticatedUserDetails currentUser, Model model) {
        if (currentUser == null || !UserRole.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/invoices";
        }

        model.addAttribute("users", userService.findUsersByUsername(username));
        model.addAttribute("searchedUsername", username);

        return "users";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserRegistrationRequestDTO());
        return "user-register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationRequestDTO userRegistrationRequestDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user-register";
        }
        UserRegistrationResponseDTO userRegistrationResponseDTO = userService.registerUser(userRegistrationRequestDTO);
        redirectAttributes.addFlashAttribute("user", userRegistrationResponseDTO);
        return "redirect:/users/register/success";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable UUID id,
                                   @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                                   RedirectAttributes redirectAttributes) {
        if (currentUser == null || !UserRole.ADMIN.equals(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("message", "Only admins can change user status");
            return "redirect:/invoices";
        }

        if (id.equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("message", "You cannot change your own status");
            return "redirect:/users";
        }

        userService.toggleUserStatus(id);
        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle-role")
    public String toggleUserRole(@PathVariable UUID id,
                                 @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                                 RedirectAttributes redirectAttributes) {
        if (currentUser == null || !UserRole.ADMIN.equals(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("message", "Only admins can change user roles");
            return "redirect:/invoices";
        }

        if (id.equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("message", "You cannot change your own role");
            return "redirect:/users";
        }

        userService.toggleUserRole(id);
        return "redirect:/users";
    }

    @GetMapping("/register/success")
    public String registerSuccess() {
        return "user-register-success";
    }

}

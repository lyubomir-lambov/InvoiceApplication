package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String showProfile(@AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                              Model model) {
        model.addAttribute("profile", userService.findUserProfile(currentUser.getId()));
        return "profile";
    }

    @GetMapping("/edit")
    public String showEditProfileForm(@AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                                      Model model) {
        model.addAttribute("profile", userService.getUserProfileForEdit(currentUser.getId()));
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String editProfile(@AuthenticationPrincipal AuthenticatedUserDetails currentUser,
                              @Valid @ModelAttribute("profile") UserProfileEditRequestDTO userProfileEditRequestDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile-edit";
        }

        try {
            userService.editUserProfile(currentUser.getId(), userProfileEditRequestDTO);
        } catch (BusinessRuleException ex) {
            bindingResult.rejectValue("email", "profile.email.duplicate", ex.getMessage());
            return "profile-edit";
        }

        redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        return "redirect:/profile";
    }
}

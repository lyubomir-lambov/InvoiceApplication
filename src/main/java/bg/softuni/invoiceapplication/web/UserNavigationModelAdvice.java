package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.security.SessionUser;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@ControllerAdvice
public class UserNavigationModelAdvice {

    private final UserService userService;

    public UserNavigationModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        UUID userId = SessionUser.getUserId(session);
        return userId != null && userService.isAdmin(userId);
    }

    @ModelAttribute("username")
    public String username(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String username = SessionUser.getUsername(session);
        if (username != null) {
            return username;
        }

        UUID userId = SessionUser.getUserId(session);
        if (userId == null) {
            return null;
        }

        username = userService.getUsernameById(userId);
        if (username != null) {
            SessionUser.setUsername(session, username);
        }

        return username;
    }
}

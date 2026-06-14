package bg.softuni.invoiceapplication.security;

import bg.softuni.invoiceapplication.model.dto.UserSessionDTO;
import bg.softuni.invoiceapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final Set<String> PUBLIC_ENDPOINTS = Set.of("/", "/login", "/users/register", "/users/register/success");
    private final Set<String> USERS_ENDPOINTS = Set.of("/invoices", "/clients");

    private final UserService userService;

    public SessionInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws IOException {

        String endpoint = httpServletRequest.getServletPath();

        if (PUBLIC_ENDPOINTS.contains(endpoint)) {
            return true;
        }

        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            httpServletResponse.sendRedirect("/login");
            return false;
        }

        UUID userId = SessionUser.getUserId(session);
        if (userId == null) {
            httpServletResponse.sendRedirect("/login");
            return false;
        }

        UserSessionDTO user = userService.getUserSessionById(userId);

        if (user == null || !user.isActive()) {
            session.invalidate();
            httpServletResponse.sendRedirect("/login");
            return false;
        }

        return true;
    }

}

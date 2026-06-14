package bg.softuni.invoiceapplication.security;

import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public class SessionUser {
    public static final String SESSION_USER_ID = "user_id";

    private SessionUser() {
    }

    public static void setUserId(HttpSession session, UUID userId) {
        session.setAttribute(SESSION_USER_ID, userId);
    }

    public static UUID getUserId(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);

        if (userId instanceof UUID uuid) {
            return uuid;
        }

        return null;
    }
}

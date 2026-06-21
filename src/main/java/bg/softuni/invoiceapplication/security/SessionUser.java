package bg.softuni.invoiceapplication.security;

import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public class SessionUser {
    public static final String SESSION_USER_ID = "user_id";
    public static final String SESSION_USERNAME = "username";

    private SessionUser() {
    }

    public static void setUserId(HttpSession session, UUID userId) {
        session.setAttribute(SESSION_USER_ID, userId);
    }

    public static void setUsername(HttpSession session, String username) {
        session.setAttribute(SESSION_USERNAME, username);
    }

    public static UUID getUserId(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);

        if (userId instanceof UUID uuid) {
            return uuid;
        }

        return null;
    }

    public static String getUsername(HttpSession session) {
        Object username = session.getAttribute(SESSION_USERNAME);

        if (username instanceof String value) {
            return value;
        }

        return null;
    }
}

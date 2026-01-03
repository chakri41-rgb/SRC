package com.genc.healthins.config;

import com.genc.healthins.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String path = request.getRequestURI();

        // Allow public resources
        if (path.startsWith("/login") || path.startsWith("/register") || path.startsWith("/assets") || path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/static") || path.equals("/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Role-based checks
        if (path.startsWith("/admin") && !"ADMIN".equals(user.getRole())) {
            response.sendRedirect("/access-denied");
            return false;
        }
        if (path.startsWith("/agent") && !"AGENT".equals(user.getRole())) {
            response.sendRedirect("/access-denied");
            return false;
        }
        if (path.startsWith("/user") && !"USER".equals(user.getRole())) {
            response.sendRedirect("/access-denied");
            return false;
        }

        // allowed for user
        return true;
    }
}

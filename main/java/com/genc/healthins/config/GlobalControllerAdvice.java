package com.genc.healthins.config;

import com.genc.healthins.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        if (session == null) return null;
        Object obj = session.getAttribute("loggedInUser");
        if (obj instanceof User) return (User) obj;
        return null;
    }
}

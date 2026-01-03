package com.genc.healthins.controller;

import com.genc.healthins.model.User;
import com.genc.healthins.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserService userService, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"/login", "/login.html"})
    public String loginPage() {
        return "login"; // templates/login.html
    }

    @GetMapping({"/", "/index", "/index.html"})
    public String homePage() {
        return "index"; // templates/index.html
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String email,
                              @RequestParam String password,
                              HttpServletRequest request,
                              RedirectAttributes ra) {
        log.debug("Login attempt for email={}", email);
        var opt = userService.findByEmail(email);
        if (opt.isEmpty()) {
            log.debug("Login failed: user not found for email={}", email);
            ra.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login";
        }
        User user = opt.get();
        // verify password using application encoder bean
        boolean ok = passwordEncoder.matches(password, user.getPassword());
        log.debug("Password check for email={} result={}", email, ok);
        if (!ok) {
            ra.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login";
        }

        // create session
        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInUser", user);

        // Role-based redirect
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        } else if ("AGENT".equals(user.getRole())) {
            return "redirect:/agent/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }

    @GetMapping({"/register", "/register.html"})
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam(required = false) String username,
                                 @RequestParam(required = false) String role,
                                 RedirectAttributes ra) {
        if (email == null || password == null) {
            ra.addFlashAttribute("error", "Please fill all required fields");
            return "redirect:/register";
        }

        if (userService.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("error", "Email already registered");
            return "redirect:/register";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role != null && !role.isBlank() ? role : "USER");
        user.setUsername(username != null && !username.isBlank() ? username : email);
        userService.save(user);
        ra.addFlashAttribute("registered", true);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() { return "not-found"; }
}

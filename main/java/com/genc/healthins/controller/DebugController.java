package com.genc.healthins.controller;

import com.genc.healthins.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private final UserService userService;

    public DebugController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/debug/users/count")
    public String usersCount() {
        return "users=" + userService.findAll().size();
    }
}
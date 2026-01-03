package com.genc.healthins.config;

import com.genc.healthins.model.User;
import com.genc.healthins.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final UserService userService;

    public DataLoader(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void loadDefaults() {
        // create default accounts if none exist
        if (userService.findByEmail("admin@hims.com").isEmpty()) {
            User admin = new User("admin@hims.com", "admin1234", "ADMIN", "Admin");
            userService.save(admin);
        }

        if (userService.findByEmail("agent@hims.com").isEmpty()) {
            User agent = new User("agent@hims.com", "agent1234", "AGENT", "Agent");
            userService.save(agent);
        }

        if (userService.findByEmail("user@hims.com").isEmpty()) {
            User u = new User("user@hims.com", "user1234", "USER", "Customer");
            userService.save(u);
        }
    }
}

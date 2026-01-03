package com.genc.healthins.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/assets/**", "/css/**", "/js/**", "/login", "/register", "/", "/not-found").permitAll()
                .anyRequest().permitAll()
            )
            .formLogin().disable()
            .logout().disable();
        return http.build();
    }
}

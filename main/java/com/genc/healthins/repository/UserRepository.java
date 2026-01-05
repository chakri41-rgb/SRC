package com.genc.healthins.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.genc.healthins.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role); // To get list of customers
    long countByRole(String role); // For Analytics
}

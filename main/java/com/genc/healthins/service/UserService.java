package com.genc.healthins.service;

import com.genc.healthins.model.User;
import java.util.Optional;

import java.util.List;

public interface UserService {
    Optional<User> findByEmail(String email);
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void deleteById(Long id);
}

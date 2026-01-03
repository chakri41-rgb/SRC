package com.genc.healthins.service;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;

import java.util.List;
import java.util.Optional;

public interface PolicyService {
    List<Policy> findAll();
    Optional<Policy> findById(Long id);
    List<Policy> findByUser(User user);
    Policy save(Policy policy);
    void deleteById(Long id);
}

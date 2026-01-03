package com.genc.healthins.service;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;

import java.util.List;
import java.util.Optional;

public interface ClaimService {
    List<Claim> findByPolicy(Policy policy);
    Optional<Claim> findById(Long id);
    Claim save(Claim claim);
    void deleteById(Long id);
    List<Claim> findAll(); 
}

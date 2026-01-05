package com.genc.healthins.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.User;
import com.genc.healthins.repository.ClaimRepository;
import com.genc.healthins.repository.UserRepository;

@Service
public class AgentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private ClaimRepository claimRepository;

    public List<User> getAllCustomers() {
        return userRepository.findByRole("CUSTOMER");
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public void processClaim(Long claimId, String newStatus) {
        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found"));
        
        logger.info("Agent changing status of Claim ID {} from {} to {}", claimId, claim.getStatus(), newStatus);
        
        claim.setStatus(newStatus);
        claimRepository.save(claim);
    }
}
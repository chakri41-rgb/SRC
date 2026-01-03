package com.genc.healthins.service.impl;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;
import com.genc.healthins.repository.ClaimRepository;
import com.genc.healthins.service.ClaimService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;

    public ClaimServiceImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public List<Claim> findByPolicy(Policy policy) { return claimRepository.findByPolicy(policy); }

    @Override
    public Optional<Claim> findById(Long id) { return claimRepository.findById(id); }

    @Override
    public Claim save(Claim claim) { return claimRepository.save(claim); }

    @Override
    public void deleteById(Long id) { claimRepository.deleteById(id); }

    @Override
    public List<Claim> findAll() { return claimRepository.findAll(); } 
}

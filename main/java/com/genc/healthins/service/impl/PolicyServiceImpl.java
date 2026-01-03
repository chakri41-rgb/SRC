package com.genc.healthins.service.impl;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import com.genc.healthins.repository.PolicyRepository;
import com.genc.healthins.service.PolicyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyServiceImpl(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public List<Policy> findAll() { return policyRepository.findAll(); }

    @Override
    public Optional<Policy> findById(Long id) { return policyRepository.findById(id); }

    @Override
    public List<Policy> findByUser(User user) { return policyRepository.findByUser(user); }

    @Override
    public Policy save(Policy policy) { return policyRepository.save(policy); }

    @Override
    public void deleteById(Long id) { policyRepository.deleteById(id); }
}

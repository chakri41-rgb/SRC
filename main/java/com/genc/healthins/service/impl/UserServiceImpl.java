package com.genc.healthins.service.impl;

import com.genc.healthins.model.User;
import com.genc.healthins.repository.UserRepository;
import com.genc.healthins.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final com.genc.healthins.repository.PolicyRepository policyRepository;
    private final com.genc.healthins.repository.ClaimRepository claimRepository;
    private final com.genc.healthins.repository.PaymentRepository paymentRepository;
    private final com.genc.healthins.repository.SupportTicketRepository supportTicketRepository;
    private final com.genc.healthins.repository.SupportResponseRepository supportResponseRepository;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           com.genc.healthins.repository.PolicyRepository policyRepository,
                           com.genc.healthins.repository.ClaimRepository claimRepository,
                           com.genc.healthins.repository.PaymentRepository paymentRepository,
                           com.genc.healthins.repository.SupportTicketRepository supportTicketRepository,
                           com.genc.healthins.repository.SupportResponseRepository supportResponseRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.supportResponseRepository = supportResponseRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        // ensure password is encoded
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        var opt = userRepository.findById(id);
        if (opt.isEmpty()) return;
        User user = opt.get();

        // delete dependent policies (and their claims/payments)
        var policies = policyRepository.findByUser(user);
        for (var p : policies) {
            var claims = claimRepository.findByPolicy(p);
            claimRepository.deleteAll(claims);
            var payments = paymentRepository.findByPolicy(p);
            paymentRepository.deleteAll(payments);
            policyRepository.delete(p);
        }

        // delete support tickets and responses
        var tickets = supportTicketRepository.findByUser(user);
        for (var t : tickets) {
            var responses = supportResponseRepository.findBySupportTicket(t);
            supportResponseRepository.deleteAll(responses);
            supportTicketRepository.delete(t);
        }

        userRepository.delete(user);
    }
}

package com.genc.healthins.service;

import com.genc.healthins.model.Payment;
import com.genc.healthins.model.Policy;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    List<Payment> findByPolicy(Policy policy);
    Optional<Payment> findById(Long id);
    Payment save(Payment payment);
}

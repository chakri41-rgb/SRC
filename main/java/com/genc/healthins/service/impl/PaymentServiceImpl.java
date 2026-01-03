package com.genc.healthins.service.impl;

import com.genc.healthins.model.Payment;
import com.genc.healthins.model.Policy;
import com.genc.healthins.repository.PaymentRepository;
import com.genc.healthins.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<Payment> findByPolicy(Policy policy) { return paymentRepository.findByPolicy(policy); }

    @Override
    public Optional<Payment> findById(Long id) { return paymentRepository.findById(id); }

    @Override
    public Payment save(Payment payment) { return paymentRepository.save(payment); }
}

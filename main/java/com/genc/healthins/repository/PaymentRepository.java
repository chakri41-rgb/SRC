package com.genc.healthins.repository;

import com.genc.healthins.model.Payment;
import com.genc.healthins.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPolicy(Policy policy);
}

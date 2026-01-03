package com.genc.healthins.repository;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicy(Policy policy);
}

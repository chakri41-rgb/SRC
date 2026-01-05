package com.genc.healthins.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.genc.healthins.model.Claim;
import com.genc.healthins.model.Policy;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicy(Policy policy);
    List<Claim> findByClaimStatus(String claimStatus);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.claimStatus = 'PENDING'")
    long countPendingClaims();

    @Query("SELECT SUM(c.claimAmount) FROM Claim c WHERE c.claimStatus = 'APPROVED'")
    Double sumApprovedClaims();
}

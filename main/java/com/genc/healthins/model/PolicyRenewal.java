package com.genc.healthins.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "policy_renewal")
public class PolicyRenewal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(name = "renewal_due_date")
    private LocalDate renewalDueDate;

    @Column(name = "renewed_at")
    private LocalDate renewedAt;

    @Column
    private String status;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }

    public LocalDate getRenewalDueDate() { return renewalDueDate; }
    public void setRenewalDueDate(LocalDate renewalDueDate) { this.renewalDueDate = renewalDueDate; }

    public LocalDate getRenewedAt() { return renewedAt; }
    public void setRenewedAt(LocalDate renewedAt) { this.renewedAt = renewedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}

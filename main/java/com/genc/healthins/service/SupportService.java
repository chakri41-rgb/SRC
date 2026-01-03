package com.genc.healthins.service;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import java.util.List;
import java.util.Optional;

public interface SupportService {
    List<SupportTicket> findByUser(User user);
    List<SupportTicket> findAll();
    SupportTicket save(SupportTicket ticket);
    long countByStatus(String status);
    long countByPriority(String priority);
    Optional<SupportTicket> findById(Long id);

}
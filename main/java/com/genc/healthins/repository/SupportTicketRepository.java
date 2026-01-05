package com.genc.healthins.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUser(User user);
    long countByTicketStatus(String ticketStatus);
    long countByPriority(String priority);
    long countByUserAndTicketStatus(User user, String status);
}

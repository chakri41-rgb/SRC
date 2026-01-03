package com.genc.healthins.repository;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUser(User user);
    long countByTicketStatus(String ticketStatus);
    
    // Fix for: "The method countByPriority is undefined"
    long countByPriority(String priority);
    
    long countByUserAndTicketStatus(User user, String status);
}

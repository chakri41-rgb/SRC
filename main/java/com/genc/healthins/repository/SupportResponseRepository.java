package com.genc.healthins.repository;

import com.genc.healthins.model.SupportResponse;
import com.genc.healthins.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportResponseRepository extends JpaRepository<SupportResponse, Long> {
    List<SupportResponse> findBySupportTicket(SupportTicket ticket);
}

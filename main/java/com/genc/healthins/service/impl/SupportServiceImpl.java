package com.genc.healthins.service.impl;

import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.model.User;
import com.genc.healthins.repository.SupportTicketRepository;
import com.genc.healthins.service.SupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository repository;

    public SupportServiceImpl(SupportTicketRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SupportTicket> findByUser(User user) {
        return repository.findByUser(user);
    }

    @Override
    public List<SupportTicket> findAll() {
        return repository.findAll();
    }

    @Override
    public SupportTicket save(SupportTicket ticket) {
        return repository.save(ticket);
    }

    @Override
    public Optional<SupportTicket> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public long countByStatus(String status) {
        // Matches the column 'ticketStatus' in your database
        return repository.countByTicketStatus(status);
    }

    @Override
    public long countByPriority(String priority) {
        return repository.countByPriority(priority);
    }
}
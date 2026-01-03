package com.genc.healthins.service.impl;

import com.genc.healthins.model.SupportResponse;
import com.genc.healthins.model.SupportTicket;
import com.genc.healthins.repository.SupportResponseRepository;
import com.genc.healthins.service.SupportResponseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportResponseServiceImpl implements SupportResponseService {

    private final SupportResponseRepository repo;

    public SupportResponseServiceImpl(SupportResponseRepository repo) {
        this.repo = repo;
    }

    @Override
    public SupportResponse save(SupportResponse response) { return repo.save(response); }

    @Override
    public List<SupportResponse> findByTicket(SupportTicket ticket) { return repo.findBySupportTicket(ticket); }
}

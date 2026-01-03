package com.genc.healthins.service;

import com.genc.healthins.model.SupportResponse;
import com.genc.healthins.model.SupportTicket;
import java.util.List;

public interface SupportResponseService {
    SupportResponse save(SupportResponse response);
    List<SupportResponse> findByTicket(SupportTicket ticket);
}
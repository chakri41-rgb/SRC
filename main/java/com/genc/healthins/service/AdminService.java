package com.genc.healthins.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.genc.healthins.model.SystemSettings;
import com.genc.healthins.repository.ClaimRepository;
import com.genc.healthins.repository.SystemSettingsRepository;
import com.genc.healthins.repository.UserRepository;

@Service
public class AdminService {
    @Autowired private UserRepository userRepository;
    @Autowired private ClaimRepository claimRepository;
    @Autowired private SystemSettingsRepository settingsRepository;

    public AdminService() {
    }

    public AdminService(ClaimRepository claimRepository, SystemSettingsRepository settingsRepository, UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", userRepository.countByRole("CUSTOMER"));
        stats.put("totalClaims", claimRepository.count());
        stats.put("pendingClaims", claimRepository.countPendingClaims());
        stats.put("totalPayout", claimRepository.sumApprovedClaims() != null ? claimRepository.sumApprovedClaims() : 0.0);
        return stats;
    }

    public List<SystemSettings> getAllSettings() {
        return settingsRepository.findAll();
    }

    public void updateSetting(String key, String value) {
        SystemSettings setting = settingsRepository.findById(key).orElse(new SystemSettings());
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        settingsRepository.save(setting);
    }

    public SystemSettingsRepository getSettingsRepository() {
        return settingsRepository;
    }

    public void setSettingsRepository(SystemSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }
}
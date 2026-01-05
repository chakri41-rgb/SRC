package com.genc.healthins.repository;

import com.genc.healthins.model.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, String> {
}
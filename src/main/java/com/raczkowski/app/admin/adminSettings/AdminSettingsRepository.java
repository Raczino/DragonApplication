package com.raczkowski.app.admin.adminSettings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSettingsRepository extends JpaRepository<AdminSetting, Long> {
    AdminSetting findBySettingKey(String settingKey);
}

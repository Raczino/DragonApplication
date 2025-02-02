package com.raczkowski.app.admin.adminSettings;

import com.raczkowski.app.admin.common.PermissionValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminSettingsService {
    private final AdminSettingsRepository adminSettingsRepository;
    private final PermissionValidator permissionValidator;
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void loadSettingsToCache() {
        List<AdminSetting> allSettings = adminSettingsRepository.findAll();
        for (AdminSetting setting : allSettings) {
            redisTemplate.opsForValue().set(setting.getSettingKey(), setting.getSettingValue());
        }
    }

    public List<AdminSetting> getSettings() {
        permissionValidator.validateIfUserIaAdmin();
        return adminSettingsRepository.findAll();
    }

    public AdminSetting getSetting(String key) {
        String settingValue = redisTemplate.opsForValue().get(key);
        if (settingValue != null) {
            return new AdminSetting(key, settingValue);
        }

        AdminSetting setting = adminSettingsRepository.findBySettingKey(key);
        if (setting != null) {
            redisTemplate.opsForValue().set(key, setting.getSettingValue());
        }
        return setting;
    }

    public void updateSettingValue(AdminSettingRequest adminSettingRequest) {
        permissionValidator.validateIfUserIaAdmin();
        AdminSetting setting = adminSettingsRepository.findBySettingKey(adminSettingRequest.getSettingKey());
        setting.setSettingValue(adminSettingRequest.getSettingValue());
        adminSettingsRepository.save(setting);

        redisTemplate.opsForValue().set(setting.getSettingKey(), setting.getSettingValue());
    }
}

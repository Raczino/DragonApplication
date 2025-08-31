package com.raczkowski.app.admin.adminSettings;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.redis.RedisService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class AdminSettingsService {
    private final AdminSettingsRepository adminSettingsRepository;
    private final PermissionValidator permissionValidator;
    private final RedisService redisService;

    @PostConstruct
    public void loadSettingsToCache() {
        List<AdminSetting> allSettings = adminSettingsRepository.findAll();
        for (AdminSetting setting : allSettings) {
            redisService.setValue(setting.getSettingKey(), setting.getSettingValue(), 1, TimeUnit.DAYS);
        }
    }

    public List<AdminSetting> getSettings() {
        if (!permissionValidator.validateAdmin())
            throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
        return adminSettingsRepository.findAll();
    }

    public AdminSetting getSetting(String key) {
        String settingValue = redisService.getValue(key);
        if (settingValue != null) {
            return new AdminSetting(key, settingValue);
        }

        AdminSetting setting = adminSettingsRepository.findBySettingKey(key);
        if (setting != null) {
            redisService.setValue(key, setting.getSettingValue(), 1, TimeUnit.DAYS);
        }
        return setting;
    }

    public void updateSettingValue(AdminSettingRequest adminSettingRequest) {
        if (!permissionValidator.validateAdmin()) {
            throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
        }
        AdminSetting setting = adminSettingsRepository.findBySettingKey(adminSettingRequest.getSettingKey());
        if (setting == null) {
            throw new ResponseException(ErrorMessages.SETTING_NOT_FOUND);
        }
        setting.setSettingValue(adminSettingRequest.getSettingValue());
        adminSettingsRepository.save(setting);

        redisService.setValue(setting.getSettingKey(), setting.getSettingValue(), 1, TimeUnit.DAYS);
    }
}

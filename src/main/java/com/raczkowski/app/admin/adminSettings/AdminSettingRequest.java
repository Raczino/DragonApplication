package com.raczkowski.app.admin.adminSettings;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSettingRequest {
    private String settingKey;
    private String settingValue;
}

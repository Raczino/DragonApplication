package com.raczkowski.app.admin.adminSettings;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webapi/v1/settings")
@AllArgsConstructor
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    @GetMapping("/get")
    ResponseEntity<List<AdminSetting>> getSettings() {
        return ResponseEntity.ok(adminSettingsService.getSettings());
    }

    @PostMapping("/update")
    void getSettings(@RequestBody AdminSettingRequest adminSettingRequest) {
        adminSettingsService.updateSettingValue(adminSettingRequest);
    }
}

package com.raczkowski.app.admin.adminSettings;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminSettingsServiceTest {

    @Mock
    private AdminSettingsRepository adminSettingsRepository;

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AdminSettingsService adminSettingsService;

    @Test
    public void shouldLoadAllSettingsIntoCacheOnInit() {
        // Given
        AdminSetting s1 = new AdminSetting("k1", "v1");
        AdminSetting s2 = new AdminSetting("k2", "v2");
        when(adminSettingsRepository.findAll()).thenReturn(List.of(s1, s2));

        // When
        adminSettingsService.loadSettingsToCache();

        // Then
        verify(redisService).setValue("k1", "v1", 1L, TimeUnit.DAYS);
        verify(redisService).setValue("k2", "v2", 1L, TimeUnit.DAYS);
        verifyNoMoreInteractions(redisService);
    }

    @Test
    public void shouldReturnAllSettingsWhenAdmin() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);
        List<AdminSetting> settings = List.of(new AdminSetting("a", "1"), new AdminSetting("b", "2"));
        when(adminSettingsRepository.findAll()).thenReturn(settings);

        // When
        List<AdminSetting> result = adminSettingsService.getSettings();

        // Then
        assertSame(settings, result);
        verify(adminSettingsRepository).findAll();
    }

    @Test
    public void shouldThrowWhenGetSettingsWithoutPermission() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(false);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> adminSettingsService.getSettings());
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());
        verifyNoInteractions(adminSettingsRepository);
    }

    @Test
    public void shouldReturnFromCacheWhenPresent() {
        // Given
        String key = "feature.x";
        when(redisService.getValue(key)).thenReturn("42");

        // When
        AdminSetting result = adminSettingsService.getSetting(key);

        // Then
        assertNotNull(result);
        assertEquals(key, result.getSettingKey());
        assertEquals("42", result.getSettingValue());
        verify(adminSettingsRepository, never()).findBySettingKey(anyString());
        verify(redisService, never()).setValue(anyString(), anyString(), anyLong(), any());
    }

    @Test
    public void shouldLoadFromRepoAndCacheWhenCacheMiss() {
        // Given
        String key = "site.title";
        when(redisService.getValue(key)).thenReturn(null);
        AdminSetting fromDb = new AdminSetting(key, "MyTitle");
        when(adminSettingsRepository.findBySettingKey(key)).thenReturn(fromDb);

        // When
        AdminSetting result = adminSettingsService.getSetting(key);

        // Then
        assertSame(fromDb, result);
        verify(redisService).setValue(key, "MyTitle", 1L, TimeUnit.DAYS);
    }

    @Test
    public void shouldReturnNullWhenNotInCacheAndNotInRepo() {
        // Given
        String key = "missing.key";
        when(redisService.getValue(key)).thenReturn(null);
        when(adminSettingsRepository.findBySettingKey(key)).thenReturn(null);

        // When
        AdminSetting result = adminSettingsService.getSetting(key);

        // Then
        assertNull(result);
        verify(redisService, never()).setValue(anyString(), anyString(), anyLong(), any());
    }

    @Test
    public void shouldThrowWhenUpdatingWithoutPermission() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(false);
        AdminSettingRequest req = mock(AdminSettingRequest.class);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class, () -> adminSettingsService.updateSettingValue(req));
        assertEquals(ErrorMessages.WRONG_PERMISSION, ex.getMessage());
        verifyNoInteractions(adminSettingsRepository, redisService);
    }

    @Test
    public void shouldUpdateSettingPersistAndCache() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);

        AdminSettingRequest req = mock(AdminSettingRequest.class);
        when(req.getSettingKey()).thenReturn("app.theme");
        when(req.getSettingValue()).thenReturn("dark");

        AdminSetting entity = new AdminSetting("app.theme", "light");
        when(adminSettingsRepository.findBySettingKey("app.theme")).thenReturn(entity);

        // When
        adminSettingsService.updateSettingValue(req);

        // Then
        assertEquals("dark", entity.getSettingValue());

        ArgumentCaptor<AdminSetting> settingCaptor = ArgumentCaptor.forClass(AdminSetting.class);
        verify(adminSettingsRepository).save(settingCaptor.capture());
        assertSame(entity, settingCaptor.getValue());
        assertEquals("dark", settingCaptor.getValue().getSettingValue());

        verify(redisService).setValue("app.theme", "dark", 1L, TimeUnit.DAYS);
    }

    @Test
    public void shouldDoNothingWhenLoadSettingsToCacheWithEmptyList() {
        // Given
        when(adminSettingsRepository.findAll()).thenReturn(List.of());

        // When
        adminSettingsService.loadSettingsToCache();

        // Then
        verifyNoInteractions(redisService);
    }

    @Test
    public void shouldThrowWhenUpdateSettingValueNotFoundInRepo() {
        // Given
        when(permissionValidator.validateAdmin()).thenReturn(true);

        AdminSettingRequest req = mock(AdminSettingRequest.class);
        when(req.getSettingKey()).thenReturn("not.exists");

        when(adminSettingsRepository.findBySettingKey("not.exists")).thenReturn(null);

        // When & Then
        ResponseException ex = assertThrows(ResponseException.class,
                () -> adminSettingsService.updateSettingValue(req));
        assertEquals(ErrorMessages.SETTING_NOT_FOUND, ex.getMessage());

        verify(adminSettingsRepository).findBySettingKey("not.exists");
        verify(adminSettingsRepository, never()).save(any());
        verify(redisService, never()).setValue(anyString(), anyString(), anyLong(), any());

        verify(req, never()).getSettingValue();
    }

}

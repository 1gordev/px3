package com.id.px3.auth.init;

import com.id.px3.auth.model.UserConfigValueType;
import com.id.px3.auth.model.entity.UserConfig;
import com.id.px3.auth.repo.UserConfigRepo;
import com.id.px3.utils.SafeConvert;
import com.id.px3.utils.excel.ExcelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class UserConfigsInitializer {

    private final AtomicBoolean initOk = new AtomicBoolean(false);
    private final ExcelConfigService excelConfigService;
    private final UserConfigRepo configRepo;

    @Value("${px3.auth.user-configs.init-from-file.enabled:false}")
    private boolean initFromFileEnabled;

    @Value("${px3.auth.user-configs.init-from-file.path:classpath:px3-auth-init.xlsx}")
    private String initFilePath;

    @Value("${px3.auth.user-configs.init-from-file.sheet:user.configs}")
    private String initSheetName;

    public UserConfigsInitializer(ExcelConfigService excelConfigService, UserConfigRepo configRepo) {
        this.excelConfigService = excelConfigService;
        this.configRepo = configRepo;
    }

    public void init() {
        if (initOk.compareAndSet(false, true)) {
            try {
                log.debug("Initializing configs");
                initConfigs();
                log.info("Configs initialized");
            } catch (Exception e) {
                log.error("Error initializing configs", e);
            }
        }
    }

    private void initConfigs() {
        if (initFromFileEnabled) {
            excelConfigService.load(initFilePath, initSheetName).stream()
                    .map(this::rowToConfig)
                    .filter(u -> !u.getCode().isBlank())
                    .forEach(u -> configRepo.findByCode(u.getCode())
                            .ifPresentOrElse(
                                    existing -> {
                                        log.debug("Config '%s' UPDATE".formatted(u.getCode()));
                                        u.setId(existing.getId());
                                        configRepo.save(u);
                                    },
                                    () -> {
                                        log.debug("Config '%s' CREATE".formatted(u.getCode()));
                                        configRepo.save(u);
                                    }));
        }
    }

    private UserConfig rowToConfig(Map<String, Object> row) {
        return new UserConfig(
                UUID.randomUUID().toString(),
                SafeConvert.toString(row.get(UserConfig.CODE)).orElse("").trim(),
                SafeConvert.toString(row.get(UserConfig.DESCRIPTION)).orElse("").trim(),
                UserConfigValueType.valueOf(
                        SafeConvert.toString(row.get(UserConfig.VALUE_TYPE))
                                .orElse(UserConfigValueType.STRING.name())
                                .trim()
                                .toUpperCase()
                )
        );
    }
}

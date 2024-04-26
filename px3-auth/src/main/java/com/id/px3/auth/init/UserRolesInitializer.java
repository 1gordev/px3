package com.id.px3.auth.init;

import com.id.px3.auth.model.entity.UserRole;
import com.id.px3.auth.repo.UserRoleRepo;
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
public class UserRolesInitializer {

    private final AtomicBoolean initOk = new AtomicBoolean(false);
    private final ExcelConfigService excelConfigService;
    private final UserRoleRepo userRoleRepo;

    @Value("${px3.auth.user-roles.init-from-file.enabled:false}")
    private boolean initFromFileEnabled;

    @Value("${px3.auth.user-roles.init-from-file.path:classpath:px3-auth-init.xlsx}")
    private String initFilePath;

    @Value("${px3.auth.user-roles.init-from-file.sheet:user.roles}")
    private String initSheetName;

    public UserRolesInitializer(ExcelConfigService excelConfigService, UserRoleRepo userRoleRepo) {
        this.excelConfigService = excelConfigService;
        this.userRoleRepo = userRoleRepo;
    }

    public void init() {
        if (initOk.compareAndSet(false, true)) {
            try {
                log.debug("Initializing roles");
                initRoles();
                log.info("Roles initialized");
            } catch (Exception e) {
                log.error("Error initializing roles", e);
            }
        }
    }

    private void initRoles() {
        if (initFromFileEnabled) {
            excelConfigService.load(initFilePath, initSheetName).stream()
                    .map(this::rowToRole)
                    .filter(u -> !u.getCode().isBlank())
                    .forEach(u -> userRoleRepo.findByCode(u.getCode())
                            .ifPresentOrElse(
                                    existing -> {
                                        log.debug("Role '%s' UPDATE".formatted(u.getCode()));
                                        u.setId(existing.getId());
                                        userRoleRepo.save(u);
                                    },
                                    () -> {
                                        log.debug("Role '%s' CREATE".formatted(u.getCode()));
                                        userRoleRepo.save(u);
                                    }));
        }
    }

    private UserRole rowToRole(Map<String, Object> row) {
        return new UserRole(
                UUID.randomUUID().toString(),
                SafeConvert.toString(row.get(UserRole.CODE)).orElse("").trim(),
                SafeConvert.toString(row.get(UserRole.DESCRIPTION)).orElse("").trim()
        );
    }
}

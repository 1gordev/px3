package com.id.px3.auth.init;

import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.repo.UserRepo;
import com.id.px3.utils.SafeConvert;
import com.id.px3.utils.excel.ExcelConfigService;
import com.id.px3.utils.sec.PasswordUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class UserInitializer {

    public static final String PASSWORD = "password";
    private final AtomicBoolean initOk = new AtomicBoolean(false);
    private final ExcelConfigService excelConfigService;
    private final UserRepo userRepo;

    @Value("${px3.auth.users.init-from-file.enabled:false}")
    private boolean initFromFileEnabled;

    @Value("${px3.auth.users.init-from-file.path:classpath:px3-auth-init.xlsx}")
    private String initFilePath;

    @Value("${px3.auth.users.init-from-file.sheet:users}")
    private String initSheetName;

    public UserInitializer(ExcelConfigService excelConfigService, UserRepo userRepo) {
        this.excelConfigService = excelConfigService;
        this.userRepo = userRepo;
    }

    public void init() {
        if (initOk.compareAndSet(false, true)) {
            try {
                log.debug("Initializing users");
                initRoot();
                initUsers();
                log.info("Users initialized");
            } catch (Exception e) {
                log.error("Error initializing users", e);
            }
        }
    }

    private void initUsers() {
        if (initFromFileEnabled) {
            excelConfigService.load(initFilePath, initSheetName).stream()
                    .map(this::rowToUser)
                    .filter(u -> !u.getUsername().isBlank() && !u.getEncPassword().isBlank())
                    .forEach(u -> userRepo.findByUsername(u.getUsername())
                            .ifPresentOrElse(
                                    existing -> {
                                        log.debug("User '%s' UPDATE".formatted(u.getUsername()));
                                        u.setId(existing.getId());
                                        userRepo.save(u);
                                    },
                                    () -> {
                                        log.debug("User '%s' CREATE".formatted(u.getUsername()));
                                        userRepo.save(u);
                                    }));
        }
    }

    private User rowToUser(Map<String, Object> row) {
        return new User(
                UUID.randomUUID().toString(),
                SafeConvert.toString(row.get(User.USERNAME)).orElse("").trim(),
                PasswordUtil.encodePassword(SafeConvert.toString(row.get(PASSWORD)).orElse("").trim()),
                new HashSet<>(SafeConvert.toStringList(row.get(User.ROLES), ";").orElse(List.of())),
                new HashMap<>(SafeConvert.toStringMap(row.get(User.CONFIG), ";", ":").orElse(Map.of()))
        );
    }

    private void initRoot() {
        //  proceed if users count is zero
        if (userRepo.count() == 0) {
            //  create default users
            userRepo.save(new User(
                    UUID.randomUUID().toString(),
                    "root",
                    PasswordUtil.encodePassword("root1234"),
                    Set.of("ROOT"),
                    Map.of()
            ));
        }

    }
}

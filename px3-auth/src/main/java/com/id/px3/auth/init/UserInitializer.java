package com.id.px3.auth.init;

import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.repo.UserRepo;
import com.id.px3.model.DefaultRoles;
import com.id.px3.utils.SafeConvert;
import com.id.px3.utils.excel.ExcelConfigService;
import com.id.px3.utils.sec.PasswordUtil;
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

    private List<String> rootUsers = new ArrayList<>();
    private List<String> rootPasswords = new ArrayList<>();

    public UserInitializer(ExcelConfigService excelConfigService,
                           UserRepo userRepo,
                           @Value("${px3.auth.users.root-users:root}") String rootUsers,
                           @Value("${px3.auth.users.root-passwords:root1234}") String rootPasswords) {
        this.excelConfigService = excelConfigService;
        this.userRepo = userRepo;
        this.rootUsers = SafeConvert.toStringList(rootUsers, ",", true).orElse(List.of());
        this.rootPasswords = SafeConvert.toStringList(rootPasswords, ",", true).orElse(List.of());
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
        String userName = SafeConvert.toString(row.get(User.USERNAME)).orElse("").trim();
        if (!SafeConvert.isAlphaNumeric(userName, false)) {
            throw new IllegalArgumentException("Invalid username: %s".formatted(userName));
        }

        return User.builder()
                .id(UUID.randomUUID().toString())
                .username(userName)
                .encPassword(PasswordUtil.encodePassword(SafeConvert.toString(row.get(PASSWORD)).orElse("").trim()))
                .roles(new HashSet<>(SafeConvert.toStringList(row.get(User.ROLES), ";", true).orElse(List.of())))
                .details(new HashMap<>(SafeConvert.toStringMap(row.get(User.DETAILS), ";", ":").orElse(Map.of())))
                .config(new HashMap<>(SafeConvert.toStringMap(row.get(User.CONFIG), ";", ":").orElse(Map.of())))
                .build();
    }

    private void initRoot() {
        //  proceed if users count is zero
        if (userRepo.count() == 0 && !rootUsers.isEmpty() && rootUsers.size() == rootPasswords.size()) {
            for (int i = 0; i < rootUsers.size(); i++) {
                String u = rootUsers.get(i);
                String p = rootPasswords.get(i);
                if (u != null && !u.isBlank() && p != null && !p.isBlank()) {
                    userRepo.save(User.builder()
                            .id(UUID.randomUUID().toString())
                            .username(u)
                            .encPassword(PasswordUtil.encodePassword(p))
                            .roles(Set.of(DefaultRoles.ROOT))
                            .build());
                }
            }
        }
    }
}

package com.id.px3.auth.init;

import com.id.px3.auth.repo.UserAccessLogRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InitOneShotRunner {

    private final ApplicationContext appCtx;

    public InitOneShotRunner(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }

    @PostConstruct
    public void run() {
        log.info("Running one-shot initialization");
        appCtx.getBean(UserRolesInitializer.class).init();
        appCtx.getBean(UserInitializer.class).init();
        appCtx.getBean(UserAccessLogRepo.class).init();
        log.info("One-shot initialization complete");
    }
}

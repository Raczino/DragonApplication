package com.raczkowski.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupEnvLogger {
    private final Environment env;

    @EventListener(ApplicationReadyEvent.class)
    void logProps() {
        log.info("Active profiles = {}", String.join(",", env.getActiveProfiles()));
        log.info("DS URL = {}", env.getProperty("spring.datasource.url"));
        log.info("Flyway URL = {}", env.getProperty("spring.flyway.url"));
        log.info("Redis = {}:{}",
                env.getProperty("spring.redis.host"),
                env.getProperty("spring.redis.port"));
    }
}

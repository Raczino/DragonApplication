package com.raczkowski.app.events;

import com.raczkowski.app.accountPremium.DeactivateSubscriptionEvent;
import com.raczkowski.app.admin.adminSettings.AdminSettingsService;
import com.raczkowski.app.article.PublishArticleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DynamicEventScheduler implements SchedulingConfigurer {

    private final AdminSettingsService settings;
    private final EventBus bus;

    private static final long DEFAULT_RATE_MS = Duration.ofMinutes(15).toMillis();
    private static final long DISABLED_POLL_MS = Duration.ofSeconds(30).toMillis();

    private Map<String, Supplier<DomainEvent>> events() {
        return Map.of(
                "publishArticles", PublishArticleEvent::new,
                "deactivateExpiredSubscriptions", DeactivateSubscriptionEvent::new
        );
    }

    @Bean
    public TaskScheduler taskScheduler() {
        var ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(Math.max(2, events().size()));
        ts.setThreadNamePrefix("sched-");
        ts.initialize();
        return ts;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar reg) {
        reg.setTaskScheduler(taskScheduler());

        events().forEach((name, factory) -> {
            final String prefix = "scheduler." + name + ".";

            reg.addTriggerTask(
                    () -> {
                        boolean enabled = settings.getBoolean(prefix + "enabled", true);
                        if (enabled) {
                            bus.publish(factory.get());
                        }
                    },
                    ctx -> {
                        boolean enabled = settings.getBoolean(prefix + "enabled", true);
                        long rateMs = Math.max(1000L, settings.getLong(prefix + "rateMs", DEFAULT_RATE_MS));

                        long last = Optional.ofNullable(ctx.lastActualExecutionTime())
                                .map(Date::getTime)
                                .orElse(System.currentTimeMillis());

                        long delay = enabled ? rateMs : DISABLED_POLL_MS;
                        return new Date(last + delay);
                    }
            );
        });
    }

}

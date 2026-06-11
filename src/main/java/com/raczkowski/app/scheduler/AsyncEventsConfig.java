package com.raczkowski.app.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncEventsConfig {
    @Bean
    public TaskExecutor eventExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setThreadNamePrefix("event-");
        ex.initialize();
        return ex;
    }

    @Bean(name = "applicationEventMulticaster")
    public SimpleApplicationEventMulticaster multicaster(TaskExecutor eventExecutor) {
        var m = new SimpleApplicationEventMulticaster();
        m.setTaskExecutor(eventExecutor);
        return m;
    }
}
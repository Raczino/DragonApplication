package com.raczkowski.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class DragonApplication {

    public static void main(String[] args) {
        SpringApplication.run(DragonApplication.class, args);
    }

}

package com.example.afr.api;

import com.example.afr.common.RecoveryRequestEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.afr")
@EntityScan(basePackageClasses = RecoveryRequestEntity.class)
@EnableJpaRepositories(basePackages = "com.example.afr.common")
public class RecoveryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecoveryApiApplication.class, args);
    }
}


package com.example.afr.dlq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.afr")
public class DlqAlertApplication {

    public static void main(String[] args) {
        SpringApplication.run(DlqAlertApplication.class, args);
    }
}


package com.medihelp360.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DatabaseSyncServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatabaseSyncServiceApplication.class, args);
    }
} 
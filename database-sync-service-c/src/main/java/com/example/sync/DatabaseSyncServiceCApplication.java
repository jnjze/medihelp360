package com.example.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del Database Sync Service C
 * Servicio de sincronización que consume eventos de Kafka y los almacena en MongoDB
 */
@SpringBootApplication
@EnableKafka
@EnableFeignClients
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
public class DatabaseSyncServiceCApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseSyncServiceCApplication.class, args);
    }
} 
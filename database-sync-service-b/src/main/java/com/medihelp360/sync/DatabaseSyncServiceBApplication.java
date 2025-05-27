package com.medihelp360.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del Database Sync Service B
 * Servicio de sincronización que consume eventos de Kafka y los almacena en MySQL
 */
@SpringBootApplication
@EnableKafka
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class DatabaseSyncServiceBApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseSyncServiceBApplication.class, args);
    }
} 
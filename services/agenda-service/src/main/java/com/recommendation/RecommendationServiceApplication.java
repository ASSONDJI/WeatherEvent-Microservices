package com.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AgendaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendaServiceApplication.class, args);
        System.out.println(" Agenda Service démarré sur http://localhost:8083");
    }
}

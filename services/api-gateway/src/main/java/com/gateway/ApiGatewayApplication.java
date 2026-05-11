package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("=".repeat(60));
        System.out.println(" API Gateway démarrée sur http://localhost:8080");
        System.out.println(" Connectée à Eureka sur http://localhost:8761");
        System.out.println("=".repeat(60));
    }
}

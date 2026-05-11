package com.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WeatherServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherServiceApplication.class, args);
		System.out.println("=".repeat(60));
		System.out.println(" Weather Service sur http://localhost:8081");
		System.out.println(" Connecté à Eureka sur http://localhost:8761");
		System.out.println("=".repeat(60));
	}
}
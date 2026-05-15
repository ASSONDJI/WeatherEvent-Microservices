package com.reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reservationOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Reservation Service API")
                .description("Service de reservation - hotel, restaurant, evenement, activite, transport")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Malaika Assondji")
                    .email("middleware@univ-cm")));
    }
}

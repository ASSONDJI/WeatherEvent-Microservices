package com.agenda.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agendaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Agenda Service API")
                .description("Service orchestrateur - Combine météo, événements et recommandations")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Malaïka Assondji")
                    .email("middleware@univ-cm")));
    }
}

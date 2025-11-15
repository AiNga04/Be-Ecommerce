package com.zyna.dev.ecommerce.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        // Định nghĩa scheme cho JWT Bearer
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Zyna E-commerce API")
                        .description("API documentation for Zyna E-commerce backend (Spring Boot + JWT)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Trương Ái Nga")
                                .email("ainga01012004@gmail.com")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuth)
                );
    }
}

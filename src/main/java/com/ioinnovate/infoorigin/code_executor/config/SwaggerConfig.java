package com.ioinnovate.infoorigin.code_executor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Python Code Quality API")
                        .version("Java Version 24")
                        .description("By Ayush Chamoli")
                        .summary("Java Spring Boot Microservice for Python Code Execution")
                        .termsOfService("https://github.com/AkshitNa")
                        .contact(new Contact()
                                .name("Team Info Origin")
                                .email("akshit.nautiyal@infoorigin.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8082")
                                .description("Production Server")
                ))
                .tags(List.of(
                        new Tag().name("Python Code Quality API")
                ));
    }
}


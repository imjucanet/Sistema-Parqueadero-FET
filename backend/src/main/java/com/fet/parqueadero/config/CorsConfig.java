package com.fet.parqueadero.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Habilita CORS para que el frontend (servido en otro origen) pueda consumir la API. */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("http://localhost:*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
                registry.addMapping("/graphql")
                        .allowedOriginPatterns("http://localhost:*")
                        .allowedMethods("POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}

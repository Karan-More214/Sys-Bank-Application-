package com.example.BankManagement.Config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Allows the React frontend (Vite dev server locally, Vercel in production) to call
 * the /api/** REST endpoints, including the Authorization header the JWT filter needs.
 *
 * This is consumed by Spring Security's .cors(Customizer.withDefaults()) in
 * SecurityConfig, not a WebMvcConfigurer - Spring Security's filter chain runs before
 * Spring MVC, so a WebMvcConfigurer-only CORS setup would not reliably apply once
 * Security sits in front of it, and running both at once risks duplicate
 * Access-Control-Allow-Origin headers (which browsers reject outright).
 *
 * FRONTEND_URL should be set to the deployed Vercel domain (e.g.
 * https://your-app.vercel.app) so production CORS works without a code change.
 */
@Configuration
public class WebConfig {

    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = new ArrayList<>();
        origins.add("http://localhost:5173");
        if (!frontendUrl.isBlank()) {
            origins.add(frontendUrl);
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}

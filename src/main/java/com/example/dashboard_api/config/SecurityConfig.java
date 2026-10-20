package com.example.dashboard_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Ainda não há autenticação: todas as rotas são públicas (`permitAll`).
 * O que limita o acesso hoje é só o CORS, restrito às origens configuradas em
 * `app.cors.origens` — o que vale para chamadas de navegador, mas não impede
 * acesso direto (curl, Postman).
 *
 * A evolução planejada é JWT nos moldes do net-tracker-api; o `java-jwt` já
 * está no pom.xml para isso.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final List<String> origensPermitidas;

    public SecurityConfig(@Value("${app.cors.origens}") List<String> origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origensPermitidas);
        config.addAllowedHeader("*");
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setExposedHeaders(List.of("x-total-content", "x-total-pages", "x-pagina-atual"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

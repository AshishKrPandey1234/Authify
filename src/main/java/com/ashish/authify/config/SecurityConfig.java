package com.ashish.authify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF because JWT tokens are stateless and safe from CSRF attacks
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configure endpoint accessibility rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to all Auth REST endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allow public access to static assets (HTML, CSS, JS) served out of /static folder
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()

                        // Any other request (like /api/home-content) MUST be authenticated
                        .anyRequest().authenticated()
                )

                // 3. Make the session STATELESS (No HTTP Session cookies saved on server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Register our Custom JWT filter BEFORE the standard UsernamePassword filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
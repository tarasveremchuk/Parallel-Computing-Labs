package com.iotplatform.device.config;

import com.iotplatform.device.security.JwtAuthenticationEntryPoint;
import com.iotplatform.device.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/internal/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/devices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/devices/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/rules").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/rules/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/groups").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/groups/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/maintenance").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/commands").hasAnyRole("ADMIN", "OPERATOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
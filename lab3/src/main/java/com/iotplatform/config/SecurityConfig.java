package com.iotplatform.config;

import com.iotplatform.security.JwtAuthenticationEntryPoint;
import com.iotplatform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/v1/auth/register", "/v1/auth/login").permitAll()
                        .requestMatchers("/v1/auth/forgot-password", "/v1/auth/reset-password").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()


                        // System health - public
                        .requestMatchers("/v1/system/health").permitAll()
                        // Users management - ADMIN only
                        .requestMatchers("/v1/users/**").hasRole("ADMIN")

                        // Devices
                        .requestMatchers(HttpMethod.POST, "/v1/devices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/devices/*/status").hasRole("ADMIN")

                        // Telemetry
                        .requestMatchers(HttpMethod.POST, "/v1/telemetry").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/v1/telemetry/batch").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/v1/telemetry/*").hasRole("ADMIN")

                        // Rules
                        .requestMatchers(HttpMethod.POST, "/v1/rules").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/rules/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/rules/*").hasRole("ADMIN")

                        // Alerts
                        .requestMatchers(HttpMethod.DELETE, "/v1/alerts/*").hasRole("ADMIN")

                        // Simulator
                        .requestMatchers(HttpMethod.POST, "/v1/simulator/**").hasRole("ADMIN")
                        // Audit - ADMIN only
                        .requestMatchers("/v1/audit/**").hasRole("ADMIN")

                        // Maintenance - ADMIN
                        .requestMatchers(HttpMethod.POST, "/v1/maintenance").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/maintenance/*/cancel").hasRole("ADMIN")

                        // Groups - ADMIN for create/update/delete
                        .requestMatchers(HttpMethod.POST, "/v1/groups").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/groups/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/groups/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/groups/*/devices/*").hasRole("ADMIN")

                        // Commands - ADMIN + OPERATOR
                        .requestMatchers(HttpMethod.POST, "/v1/commands").hasAnyRole("ADMIN", "OPERATOR")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
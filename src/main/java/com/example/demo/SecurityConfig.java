package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserRepository appUserRepository;
    private final JwtFilter jwtFilter;

    public SecurityConfig(AppUserRepository appUserRepository, JwtFilter jwtFilter) {
        this.appUserRepository = appUserRepository;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> appUserRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/task/test/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/index.html", "/", "/*.html", "/*.js", "/*.css").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/task/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/task/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/task/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/task/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/task/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
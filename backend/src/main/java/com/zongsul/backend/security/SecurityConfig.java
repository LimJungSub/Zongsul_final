package com.zongsul.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // ============================
        // 1) CORS 허용
        // ============================
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("*"));
            config.setAllowedMethods(List.of("*"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(false);
            return config;
        }));

        // ============================
        // 2) CSRF, 세션 off
        // ============================
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // ============================
        // 3) 공개 API 설정 (permitAll)
        // ============================
        http.authorizeHttpRequests(auth -> auth

                // OPTIONS 허용 (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 손님/관리자 공통 (JWT 없이 접근 가능)
                .requestMatchers("/distribution/**").permitAll() // ⭐ claim 포함
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/dishes/**").permitAll()

                // 문서
                .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // 업로드 등 기타
                .requestMatchers("/upload/**").permitAll()

                // 나머지는 인증 필요
                .anyRequest().authenticated()
        );

        // ============================
        // 4) JWT 필터 추가
        // ============================
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}

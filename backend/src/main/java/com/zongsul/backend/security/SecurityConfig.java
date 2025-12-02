package com.zongsul.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS 기본값 적용
                .cors(Customizer.withDefaults())
                // CSRF 끄기
                .csrf(csrf -> csrf.disable())
                // 세션 안쓴다고 명시(선택 사항이지만 깔끔하게)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ✅ 모든 요청 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        // ✅ JwtAuthenticationFilter 같은 거 일단 안 끼움
        return http.build();
    }
}

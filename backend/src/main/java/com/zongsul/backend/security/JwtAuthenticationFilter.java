package com.zongsul.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth") ||           // 로그인 / 회원가입
                path.startsWith("/distribution") ||        // 배포
                path.startsWith("/api/dishes") ||          // 식단
                path.startsWith("/upload") ||              // 이미지 업로드
                path.equals("/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")
        ) {
            filterChain.doFilter(request, response);
            return;
        }


        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (auth != null && auth.startsWith("Bearer ")) {

            String token = auth.substring(7);

            try {
                Claims claims = tokenProvider.parse(token);

                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                var authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                var authToken =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (Exception ex) {
                // 토큰 잘못된 경우 → 인증 없이 계속 진행
            }
        }


        filterChain.doFilter(request, response);
    }
}

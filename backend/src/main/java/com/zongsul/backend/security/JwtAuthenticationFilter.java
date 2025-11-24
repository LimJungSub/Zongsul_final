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

/**
 * JwtAuthenticationFilter
 * - 특정 API는 JWT를 검사하지 않고 바로 다음 필터로 넘긴다.
 * - 그 외 API는 Authorization 헤더의 JWT를 파싱하여 인증정보를 설정한다.
 */
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

        // *** 🚀 핵심 수정: getRequestURI() 로 바꿔야 경로가 정확히 읽힘 ***
        String path = request.getRequestURI();

        // ============================
        // 1) JWT 인증을 건너뛸 공개 API 등록
        // ============================
        if (path.startsWith("/distribution")     // claim 포함
                || path.startsWith("/api/dishes")
                || path.startsWith("/upload")
                || path.startsWith("/api/auth")
                || path.equals("/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // ============================
        // 2) JWT 인증 처리
        // ============================
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

        // ============================
        // 3) 다음 필터로 진행
        // ============================
        filterChain.doFilter(request, response);
    }
}

package com.zongsul.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtTokenProvider
 * - 로그인 성공 시 토큰을 생성하고, 요청 시 토큰을 검증/파싱합니다.
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMs;

    public JwtTokenProvider(
            @Value("${security.jwt.secret:change-me-please-very-secret-key-for-dev}") String secret,
            @Value("${security.jwt.validity-ms:86400000}") long validityInMs) {
        // HS256 서명에 사용할 키 생성 (dev 용도)
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMs = validityInMs;
    }

public String createToken(String subject, String role, String name) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityInMs);
        return Jwts.builder()
                .setSubject(subject) // 이메일
                .claim("role", role)
                .claim("name", name)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }
}

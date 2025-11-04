package com.zongsul.backend.domain.user;

/**
 * 사용자 역할
 * - Spring Security에서 ROLE_ 접두사가 자동으로 붙으므로 문자열은 ADMIN/USER로 관리합니다.
 */
public enum UserRole {
    ADMIN, USER
}

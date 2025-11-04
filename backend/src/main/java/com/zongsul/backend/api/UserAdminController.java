package com.zongsul.backend.api;

import com.zongsul.backend.domain.user.User;
import com.zongsul.backend.domain.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 목록 조회 엔드포인트
 * - 목적  프론트엔드 관리 화면에서 전체 사용자 목록을 표 형태로 보여주기 위함
 * - 권한  관리자만 접근 가능  JWT 토큰에 ROLE_ADMIN 이 있어야 통과시키기 위해,  @PreAuthorize 로 강제
 * - 데이터 소스  JPA UserRepository  DB users 테이블을 조회해서 필요한 필드만 가벼운 요약 DTO로 매핑
 * - 트래픽  목록이 커질 수 있으므로 페이징 확장이 쉬운 형태로 작성  향후 Pageable 인자만 추가하면 됨
 */
@RestController
@RequestMapping("/api")
public class UserAdminController {

    private final UserRepository userRepository;

    public UserAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 전체 목록을 반환함
     * - 현재는 단순 전체 조회 후 id  email  name  role  createdAt 만 내려보냄
     * - 프론트가 리스트 렌더링에 바로 쓰도록 가벼운 DTO를 사용함
     * - 보안상 비밀번호 등 민감 정보는 포함하지 않음
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummary> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(u -> new UserSummary(u.getId(), u.getEmail(), u.getName(), u.getRole().name(), u.getCreatedAt()))
                .toList();
    }

    /**
     * 사용자 목록 응답 전용 DTO
     * - 엔티티 전체를 노출하지 않고 필요한 최소 필드만 포함
     * - createdAt 은 테이블 정렬이나 표시용으로 활용 가능
     */
    public record UserSummary(
            Long id,
            String email,
            String name,
            String role,
            LocalDateTime createdAt
    ) {}
}

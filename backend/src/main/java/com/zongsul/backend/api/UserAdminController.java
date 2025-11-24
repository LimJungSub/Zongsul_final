package com.zongsul.backend.api;

import com.zongsul.backend.domain.user.User;
import com.zongsul.backend.domain.user.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserAdminController {

    private final UserRepository userRepository;

    public UserAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 🔹 사용자 전체 목록 조회
    @GetMapping("/users")
    public List<UserSummary> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(u -> new UserSummary(
                        u.getId(),
                        u.getName(),
                        u.getStudentId(),
                        u.getCreatedAt()
                ))
                .toList();
    }

    // 🔹 응답 DTO
    public record UserSummary(
            Long id,
            String name,
            String studentId,
            LocalDateTime createdAt
    ) {}
}

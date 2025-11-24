package com.zongsul.backend.api;

import com.zongsul.backend.domain.user.User;
import com.zongsul.backend.domain.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin(origins ="*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String name = req.get("name");
        String studentId = req.get("studentId");

        if (name == null || studentId == null || name.trim().isEmpty() || studentId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이름과 학번을 모두 입력하세요."));
        }

        // DB에서 학번으로 조회
        User user = userRepository.findByStudentId(studentId).orElse(null);

        if (user == null) {
            // 등록되지 않은 학번이면 새로 저장
            user = new User(name, studentId);
            userRepository.save(user);
        } else if (!user.getName().equals(name)) {
            // 같은 학번에 다른 이름이면 오류
            return ResponseEntity.status(400).body(Map.of("error", "이미 다른 이름으로 등록된 학번입니다."));
        }

        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",
                "name", user.getName(),
                "studentId", user.getStudentId()
        ));
    }
}

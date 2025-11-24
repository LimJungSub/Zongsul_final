package com.zongsul.backend.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * User 엔티티 (이름 + 학번 기반 단순 로그인용)
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_user_studentId", columnNames = "student_id"))
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 사용자 이름

    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId; // 학번 (고유)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}

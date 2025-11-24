package com.zongsul.backend.domain.distribution;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DistributionClaim
 * - 세션별 수령 사용자 1회 제한 (session_id + student_id 유니크)
 * - 프론트/백엔드 공통 키(name, studentId) 사용
 */
@Entity
@Table(
        name = "distribution_claim",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_claim_session_student",
                columnNames = {"session_id", "student_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DistributionClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DistributionSession session;

    // 🔹 프론트/백엔드 공통 키로 통일: name
    @Column(name = "user_name", nullable = false, length = 100)
    private String name;

    // 🔹 동명이인 구분: studentId 추가 (권장)
    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    public DistributionClaim(DistributionSession session, String name, String studentId) {
        this.session = session;
        this.name = name;
        this.studentId = studentId;
        this.claimedAt = LocalDateTime.now();
    }
    public DistributionClaim(DistributionSession session, String name) {
        this.session = session;
        this.name = name;
        this.studentId = "UNKNOWN"; // or null (if you prefer)
        this.claimedAt = LocalDateTime.now();
    }
    // ✅ 기존 코드 호환용 (예전에 userName을 참조하던 코드가 있을 수 있어서 유지)
    public String getUserName() { return this.name; }
    public void setUserName(String userName) { this.name = userName; }
}

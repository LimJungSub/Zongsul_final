package com.zongsul.backend.domain.distribution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DistributionSession
 * - 반찬 배포 세션
 * - capacity만큼 remainingCount 초기화
 * - claim 발생 시 1씩 감소
 * - version 기반 낙관적 락 적용
 */
@Entity
@Table(name = "distribution_session")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DistributionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "remaining_count", nullable = false)
    private Integer remainingCount;

    @Column(nullable = false)
    private Boolean active = true;

    @Version
    private Integer version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"session"})
    private List<DistributionClaim> claims = new ArrayList<>();

    public DistributionSession(String menuName, Integer capacity) {
        this.menuName = menuName;
        this.capacity = capacity;
        this.remainingCount = capacity;
        this.active = true;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

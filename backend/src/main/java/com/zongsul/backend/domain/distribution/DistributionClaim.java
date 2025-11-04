package com.zongsul.backend.domain.distribution;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DistributionClaim 엔티티
 * - 특정 세션에서 사용자(userName)가 한 번만 수령하도록 제약(세션+사용자 UNIQUE)이 적용됩니다.
 * - 선착순 소진 시도 시 동시성으로 인한 중복 수령 방지에 기여합니다.
 */
@Entity
@Table(name = "distribution_claim")
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

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    public DistributionClaim(DistributionSession session, String userName) {
        this.session = session;
        this.userName = userName;
        this.claimedAt = LocalDateTime.now();
    }
}

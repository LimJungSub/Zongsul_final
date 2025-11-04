package com.zongsul.backend.domain.food;

import com.zongsul.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * FoodRequest(수령 요청)
 * - 한 사용자가 특정 Food에 대해 수령 의사를 밝히고, 관리자 확인 후 상태가 변경됩니다.
 * - (food_id, user_id) 유니크 제약으로 중복 신청을 방지합니다.
 */
@Entity
@Table(name = "food_request",
       uniqueConstraints = @UniqueConstraint(name = "uq_food_user", columnNames = {"food_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class FoodRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pickup_time")
    private LocalTime pickupTime; // 사용자가 지정한 수령 예정 시각(선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // REQUESTED, COMPLETED, CANCELED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FoodRequest(Food food, User user, LocalTime pickupTime) {
        this.food = food;
        this.user = user;
        this.pickupTime = pickupTime;
        this.status = RequestStatus.REQUESTED;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}

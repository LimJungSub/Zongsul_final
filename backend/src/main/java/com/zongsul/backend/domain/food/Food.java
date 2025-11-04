package com.zongsul.backend.domain.food;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Food(남은 음식) 엔티티
 * - 관리자가 당일 남은 메뉴를 등록하고, 사용자들이 수령 요청을 올립니다.
 * - remainingCount는 capacity에서 요청 생성 시 1씩 감소하여 예약을 보장합니다.
 * - 낙관적 락 @Version으로 동시성 하에서 안전한 차감을 지원합니다.
 */
@Entity
@Table(name = "food")
@Getter
@Setter
@NoArgsConstructor
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String menu;

    @Column(nullable = false)
    private Integer capacity; // 최초 등록 수량

    @Column(name = "remaining_count", nullable = false)
    private Integer remainingCount; // 남은 수령 가능 수량

    @Column(nullable = false)
    private LocalDate date; // 제공 날짜

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodStatus status; // AVAILABLE, CLOSED

    @Version
    private Integer version; // 낙관적 락 버전

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodRequest> requests = new ArrayList<>();

    public Food(String menu, int capacity, LocalDate date, String note) {
        this.menu = menu;
        this.capacity = capacity;
        this.remainingCount = capacity;
        this.date = date;
        this.note = note;
        this.status = FoodStatus.AVAILABLE;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}

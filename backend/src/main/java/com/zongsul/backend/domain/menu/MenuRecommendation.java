package com.zongsul.backend.domain.menu;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MenuRecommendation 엔티티
 * - 요일별(월~금) 추천 식단 조합 저장
 * - 주차 구분은 "weekStartDate(월요일)"로 표현
 *
 * 실무 팁
 * - 동일 주에 대한 중복 생성 방지를 위해 (week_start_date, day_of_week) 유니크 인덱스 추가 가능
 *   여기서는 조회 인덱스(idx_menu_rec_week_day)만 생성해둠
 */
@Entity
@Table(name = "menu_recommendation")
@Getter
@Setter
@NoArgsConstructor
public class MenuRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    /**
     * ISO 요일 값을 저장 (1=월 .. 7=일)
     * 프런트는 보통 월~금만 사용
     */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    // 1. 밥
    @Column(nullable = false)
    private String rice;

    // 2. 국
    @Column(nullable = false)
    private String soup;

    // 3. 메인 반찬
    @Column(nullable = false)
    private String main;

    // 4. 서브 메뉴
    @Column(nullable = false)
    private String side1;

    // 5. 나물류
    @Column(nullable = false)
    private String side2;

    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}

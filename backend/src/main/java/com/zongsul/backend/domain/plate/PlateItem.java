package com.zongsul.backend.domain.plate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PlateItem 엔티티
 * - 한 식판(PlateResult)에 속한 개별 음식 항목과 남김율(0.0~1.0)을 표현합니다.
 *
 * 매핑 상세
 * 1) @ManyToOne(fetch = LAZY)
 *    - 다:1 구조로 PlateResult에 연결됩니다. 지연 로딩을 통해 필요 시에만 부모를 로딩합니다.
 * 2) remainingRatio
 *    - 0.0(하나도 안 남김) ~ 1.0(모두 남김) 범위를 가정합니다. DB 타입은 DOUBLE.
 *    - 비즈니스 검증은 서비스/컨트롤러 단에서 수행 가능합니다.
 * 3) food
 *    - 음식 카테고리/이름(예: "rice", "kimchi")를 소문자 스네이크케이스로 저장 가정.
 */
@Entity
@Table(name = "plate_item")
@Getter
@Setter
@NoArgsConstructor
public class PlateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_result_id", nullable = false)
    private PlateResult plateResult;

    @Column(name = "food", nullable = false, length = 100)
    private String food;

    @Column(name = "remaining_ratio", nullable = false)
    private Double remainingRatio;

    public PlateItem(String food, Double remainingRatio) {
        this.food = food;
        this.remainingRatio = remainingRatio;
    }
}

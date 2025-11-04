package com.zongsul.backend.domain.plate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PlateResult 엔티티
 * - 한 명(한 식판)의 AI 분석 결과를 표현하는 루트 엔티티입니다.
 * - plate_result 테이블과 매핑되며, 1:N으로 PlateItem(음식 단위 남김율)과 연관됩니다.
 *
 * 주요 설계 포인트
 * 1) @OneToMany(mappedBy = "plateResult", cascade = ALL, orphanRemoval = true)
 *    - 부모(PlateResult) 저장 시 자식(PlateItem)도 함께 저장되도록 CASCADE를 설정했습니다.
 *    - 부모에서 자식 컬렉션에서 제거하면 DB에서도 삭제(orphanRemoval=true)되도록 했습니다.
 *    - 실무에서 대량 업로드 시 컬렉션 조작 비용을 최소화하기 위해 영속성 전이로 단순화합니다.
 * 2) @PrePersist로 createdAt 자동 세팅
 *    - DB DEFAULT CURRENT_TIMESTAMP와 중복되지 않게 애플리케이션 차원에서도 보장합니다.
 * 3) 양방향 연관관계 편의 메서드 addItem
 *    - item.setPlateResult(this)와 items.add(item)를 함께 수행해 일관성을 지킵니다.
 *
 * 주의사항
 * - 대규모 INSERT 성능이 중요하면 batch insert 설정(hibernate.jdbc.batch_size) 고려.
 * - createdAt 기준 조회가 많으므로 인덱스(idx_plate_result_created_at)를 Flyway에서 생성했습니다.
 */
@Entity
@Table(name = "plate_result")
@Getter
@Setter
@NoArgsConstructor
public class PlateResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업로드한 원본 파일명 등 외부 식별자
     * nullable 허용(필요 시 NOT NULL 제약 추가 가능)
     */
    @Column(name = "plate_id")
    private String plateId;

    /**
     * 레코드 생성 시각
     * - 애플리케이션에서 세팅하며, DB에도 DEFAULT 존재
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 음식별 남김율 항목 컬렉션
     * - 지연 로딩(LAZY)이 기본이지만, 여기서는 컬렉션 기본 FetchType.LAZY
     */
    @OneToMany(mappedBy = "plateResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlateItem> items = new ArrayList<>();

    public PlateResult(String plateId) {
        this.plateId = plateId;
    }

    /**
     * 연관관계 편의 메서드: 양방향 연관관계의 양쪽 값을 모두 세팅
     */
    public void addItem(PlateItem item) {
        item.setPlateResult(this);
        this.items.add(item);
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

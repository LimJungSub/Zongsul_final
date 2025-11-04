package com.zongsul.backend.domain.plate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PlateItemRepository
 * - PlateItem CRUD + 통계성(기간 평균) 쿼리를 제공합니다.
 * - JPQL을 사용하여 기간(start~end) 사이 PlateResult.createdAt 기준으로 그룹핑 평균을 계산합니다.
 *
 * 주의: createdAt 인덱스가 있어 조회 성능 보조 (Flyway에 생성해둠)
 */
public interface PlateItemRepository extends JpaRepository<PlateItem, Long> {

    /**
     * 인터페이스 기반 Projection
     * - select 절의 alias와 getter 이름을 매칭하여 바로 DTO 없이 결과를 매핑합니다.
     */
    interface FoodAvgProjection {
        String getFood();
        Double getAvgRatio();
    }

    /**
     * 특정 기간(start <= createdAt < end) 동안 음식별 남김율 평균을 계산하는 JPQL
     * - entity graph: PlateItem -> PlateResult(createdAt)
     * - 집계 결과는 FoodAvgProjection으로 반환
     */
    @Query("select pi.food as food, avg(pi.remainingRatio) as avgRatio " +
           "from PlateItem pi " +
           "where pi.plateResult.createdAt >= :start and pi.plateResult.createdAt < :end " +
           "group by pi.food")
    List<FoodAvgProjection> avgByFoodBetween(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}

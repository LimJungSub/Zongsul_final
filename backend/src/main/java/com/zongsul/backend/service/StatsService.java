package com.zongsul.backend.service;

import com.zongsul.backend.domain.plate.PlateItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StatsService
 * - 날짜 단위로 음식별 남김율 평균을 집계합니다.
 * - JPA Projection을 사용하여 최소한의 데이터만 조회합니다.
 */
@Service
public class StatsService {

    private final PlateItemRepository plateItemRepository;

    public StatsService(PlateItemRepository plateItemRepository) {
        this.plateItemRepository = plateItemRepository;
    }

    /**
     * 주어진 날짜 00:00:00 ~ 다음날 00:00:00 구간의 평균 남김율을 계산
     */
    public Map<String, Double> averagesForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<PlateItemRepository.FoodAvgProjection> rows = plateItemRepository.avgByFoodBetween(start, end);
        Map<String, Double> map = new HashMap<>();
        for (PlateItemRepository.FoodAvgProjection r : rows) {
            map.put(r.getFood(), r.getAvgRatio());
        }
        return map;
    }
}

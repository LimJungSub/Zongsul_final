package com.zongsul.backend.domain.food;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * FoodRepository
 * - 날짜/상태 기반 조회 메서드 제공
 */
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByDate(LocalDate date);
    List<Food> findByStatus(FoodStatus status);
}

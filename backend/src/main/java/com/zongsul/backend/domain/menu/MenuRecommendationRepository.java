package com.zongsul.backend.domain.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * MenuRecommendationRepository
 * - 주차(weekStartDate)별로 요일 순으로 식단 조합을 조회합니다.
 */
public interface MenuRecommendationRepository extends JpaRepository<MenuRecommendation, Long> {
    List<MenuRecommendation> findByWeekStartDateOrderByDayOfWeekAsc(LocalDate weekStartDate);
    boolean existsByWeekStartDate(LocalDate weekStartDate);
}

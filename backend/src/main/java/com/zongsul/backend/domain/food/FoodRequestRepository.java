package com.zongsul.backend.domain.food;

import com.zongsul.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * FoodRequestRepository
 */
public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {
    boolean existsByFoodIdAndUserId(Long foodId, Long userId);
    List<FoodRequest> findByFoodId(Long foodId);
    List<FoodRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}

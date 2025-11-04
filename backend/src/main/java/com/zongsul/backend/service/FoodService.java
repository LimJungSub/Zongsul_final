package com.zongsul.backend.service;

import com.zongsul.backend.domain.food.*;
import com.zongsul.backend.domain.user.User;
import com.zongsul.backend.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * FoodService
 * - 남은 음식 등록/조회/삭제와 수령 요청 생성/완료 처리 담당
 */
@Service
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodRequestRepository foodRequestRepository;
    private final UserRepository userRepository;

    public FoodService(FoodRepository foodRepository, FoodRequestRepository foodRequestRepository, UserRepository userRepository) {
        this.foodRepository = foodRepository;
        this.foodRequestRepository = foodRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Food createFood(String menu, int quantity, LocalDate date, String note) {
        Food f = new Food(menu, quantity, date, note);
        return foodRepository.save(f);
    }

    public List<Food> listByDate(LocalDate date) { return foodRepository.findByDate(date); }

    public List<Food> listAvailable() { return foodRepository.findByStatus(FoodStatus.AVAILABLE); }

    public Food get(Long id) { return foodRepository.findById(id).orElse(null); }

    @Transactional
    public void delete(Long id) { foodRepository.deleteById(id); }

    /**
     * 사용자의 수령 요청 등록
     * - 남은 수량을 1 감소시키며, 0이 되면 상태를 CLOSED로 변경해 더 이상 신청 불가
     */
    @Transactional
    public FoodRequest request(Long foodId, Long userId, LocalTime pickupTime) {
        Food food = foodRepository.findById(foodId).orElse(null);
        if (food == null || food.getStatus() != FoodStatus.AVAILABLE) return null;
        if (food.getRemainingCount() <= 0) { food.setStatus(FoodStatus.CLOSED); return null; }
        if (foodRequestRepository.existsByFoodIdAndUserId(foodId, userId)) return null;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        food.setRemainingCount(food.getRemainingCount() - 1);
        if (food.getRemainingCount() <= 0) food.setStatus(FoodStatus.CLOSED);
        return foodRequestRepository.save(new FoodRequest(food, user, pickupTime));
    }

    /**
     * 요청 상태를 COMPLETED로 변경 (관리자)
     */
    @Transactional
    public FoodRequest complete(Long foodId, Long userId) {
        List<FoodRequest> list = foodRequestRepository.findByFoodId(foodId);
        for (FoodRequest fr : list) {
            if (fr.getUser().getId().equals(userId)) {
                fr.setStatus(RequestStatus.COMPLETED);
                return fr;
            }
        }
        return null;
    }

    public List<FoodRequest> listRequests(Long foodId) { return foodRequestRepository.findByFoodId(foodId); }

    public List<FoodRequest> userRequests(Long userId) { return foodRequestRepository.findByUserIdOrderByCreatedAtDesc(userId); }
}

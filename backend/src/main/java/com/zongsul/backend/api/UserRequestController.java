package com.zongsul.backend.api;

import com.zongsul.backend.service.FoodService;
import com.zongsul.backend.domain.food.FoodRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRequestController {

    private final FoodService foodService;

    public UserRequestController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/{userId}/requests")
    public List<FoodRequest> myRequests(@PathVariable Long userId) {
        return foodService.userRequests(userId);
    }
}

package com.zongsul.backend.api;

import com.zongsul.backend.domain.food.*;
import com.zongsul.backend.service.FoodService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // 관리자: 남은 음식 등록
    public record CreateFoodRequest(@NotBlank String menu, @Min(1) int quantity,
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    String note) {}

    @PostMapping("/foods")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createFood(@RequestBody CreateFoodRequest req) {
        Food f = foodService.createFood(req.menu(), req.quantity(), req.date(), req.note());
        return ResponseEntity.ok(f);
    }

    // 목록 조회: ?date=YYYY-MM-DD 또는 ?status=available
    @GetMapping("/foods")
    public List<Food> listFoods(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(required = false) String status) {
        if (date != null) return foodService.listByDate(date);
        if (status != null && status.equalsIgnoreCase("available")) return foodService.listAvailable();
        return foodService.listAvailable(); // 기본 available
    }

    @GetMapping("/foods/{id}")
    public ResponseEntity<?> getFood(@PathVariable Long id) {
        Food f = foodService.get(id);
        return (f == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(f);
    }

    @DeleteMapping("/foods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFood(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 관리자: 수령 희망자 목록
    @GetMapping("/foods/{id}/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FoodRequest> listRequests(@PathVariable Long id) { return foodService.listRequests(id); }

    // 관리자: 수령 완료 표시
    public record CompleteRequest(String status) {}

    @PatchMapping("/foods/{id}/requests/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> complete(@PathVariable Long id, @PathVariable Long userId, @RequestBody CompleteRequest req) {
        if (req.status() == null || !req.status().equalsIgnoreCase("completed"))
            return ResponseEntity.badRequest().body(Map.of("error","status must be 'completed'"));
        FoodRequest fr = foodService.complete(id, userId);
        return (fr == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(fr);
    }

    // 사용자: 수령 요청 등록
    public record RequestCreate(Long userId, String pickupTime) {}

    @PostMapping("/foods/{id}/requests")
    public ResponseEntity<?> request(@PathVariable Long id, @RequestBody RequestCreate req) {
        LocalTime time = (req.pickupTime() == null) ? null : LocalTime.parse(req.pickupTime());
        FoodRequest fr = foodService.request(id, req.userId(), time);
        return (fr == null) ? ResponseEntity.badRequest().body(Map.of("error","cannot request")) : ResponseEntity.ok(fr);
    }
}

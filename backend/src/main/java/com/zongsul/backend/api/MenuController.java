package com.zongsul.backend.api;

import com.zongsul.backend.domain.menu.MenuRecommendation;
import com.zongsul.backend.domain.menu.MenuRecommendationRepository;
import com.zongsul.backend.service.MenuRecommendationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuRecommendationRepository repo;
    private final MenuRecommendationService service;

    public MenuController(MenuRecommendationRepository repo, MenuRecommendationService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping("/next-week")
    public List<MenuRecommendation> recommendations(
            @RequestParam(value = "weekStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        LocalDate target = (weekStart == null) ? service.targetWeekStartDate() : weekStart;
        return repo.findByWeekStartDateOrderByDayOfWeekAsc(target);
    }


    public record MenuAnalysisRequest(List<String> menus) {}

    @PostMapping("/analyze")
    public Map<String, Double> analyze(@RequestBody MenuAnalysisRequest request) {
        return service.analyzeMenu(request.menus());
    }

    @PostMapping("/recommend-substitute")
    public Map<String, String> recommendSubstitute(@RequestBody MenuAnalysisRequest request) {
        return service.recommendSubstitute(request.menus());
    }

    /**
     * 분석된 반찬(월~금)을 기반으로 다음주 식단표 생성
     * 프론트에서 forcedSides(Map<String, String>)를 보내면
     * 해당 서브반찬을 요일별로 강제로 넣어준다.
     */
    @PostMapping("/next-week")
    public List<MenuRecommendation> generateNextWeek(@RequestBody Map<String, String> forcedSides) {
        return service.generateNextWeekWithForcedSides(forcedSides);
    }

}

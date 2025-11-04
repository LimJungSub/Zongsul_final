package com.zongsul.backend.api;

import com.zongsul.backend.domain.menu.MenuRecommendation;
import com.zongsul.backend.domain.menu.MenuRecommendationRepository;
import com.zongsul.backend.service.MenuRecommendationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/ai_recommendations")
    public List<MenuRecommendation> recommendations(
            @RequestParam(value = "weekStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        LocalDate target = (weekStart == null) ? service.targetWeekStartDate() : weekStart;
        return repo.findByWeekStartDateOrderByDayOfWeekAsc(target);
    }

    @GetMapping("/substitute")
    public Map<String, String> substitute() {
        return Map.of(
                "spinach_namul", "bean_sprouts",
                "kimchi", "radish_kimchi",
                "miso_soup", "seaweed_soup"
        );
    }
}

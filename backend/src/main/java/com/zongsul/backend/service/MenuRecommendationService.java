package com.zongsul.backend.service;

import com.zongsul.backend.domain.menu.MenuRecommendation;
import com.zongsul.backend.domain.menu.MenuRecommendationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * MenuRecommendationService
 * - 최근 7일 남김율을 참고하여 다다음주(다음주 다음) 식단을 자동 생성합니다.
 * - 높은 남김율(>=0.6) 항목은 제외하거나 대체 맵핑을 적용합니다.
 */
@Service
public class MenuRecommendationService {

    private final MenuRecommendationRepository repo;
    private final StatsService statsService;

    public MenuRecommendationService(MenuRecommendationRepository repo, StatsService statsService) {
        this.repo = repo;
        this.statsService = statsService;
    }

    /**
     * 매주 일요일 00:00에 생성 시도 (이미 있으면 스킵)
     */
    @Scheduled(cron = "0 0 0 ? * SUN")
    public void generateWeeklyIfNeeded() {
        LocalDate targetWeek = targetWeekStartDate();
        if (repo.existsByWeekStartDate(targetWeek)) return;
        generateForWeek(targetWeek);
    }

    /**
     * 다다음주 월요일(= next Monday + 1주)을 계산
     */
    public LocalDate targetWeekStartDate() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        return nextMonday.plusWeeks(1);
    }

    /**
     * 주어진 주차(월요일 시작)에 대해 Mon..Fri 5개 조합을 생성
     */
    public void generateForWeek(LocalDate weekStart) {
        Map<String, Double> last7 = aggregateLastDays(7);

        // 평균 남김율 0.6 이상이면 제외 대상
        Set<String> highWaste = new HashSet<>();
        last7.forEach((food, avg) -> { if (avg != null && avg >= 0.6) highWaste.add(food); });

        // 단순 풀(데모용)
        List<String> mains = List.of("pork_bulgogi", "fish_cutlet", "chicken_stew", "beef_bowl", "spicy_pork");
        List<String> sidesA = List.of("kimchi", "spinach_namul", "bean_sprouts", "pickled_radish", "tofu");
        List<String> sidesB = List.of("egg_roll", "stir_fried_anchovy", "potato_salad", "seaweed_soup", "miso_soup");

        Map<String, String> substitute = Map.of(
                "spinach_namul", "bean_sprouts",
                "kimchi", "radish_kimchi",
                "miso_soup", "seaweed_soup"
        );

        Random random = new Random();
        List<MenuRecommendation> out = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Mon..Fri
            String main = choosePreferLowWaste(mains, highWaste, substitute, random);
            String s1 = choosePreferLowWaste(sidesA, highWaste, substitute, random);
            String s2 = choosePreferLowWaste(sidesB, highWaste, substitute, random);
            MenuRecommendation mr = new MenuRecommendation();
            mr.setWeekStartDate(weekStart);
            mr.setDayOfWeek(DayOfWeek.MONDAY.plus(i).getValue());
            mr.setMain(main);
            mr.setSide1(s1);
            mr.setSide2(s2);
            mr.setNotes("auto-generated");
            out.add(mr);
        }
        repo.saveAll(out);
    }

    private Map<String, Double> aggregateLastDays(int days) {
        Map<String, Double> acc = new HashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = LocalDate.now().minusDays(i);
            Map<String, Double> m = statsService.averagesForDate(d);
            for (var e : m.entrySet()) {
                acc.merge(e.getKey(), e.getValue(), (a, b) -> (a + b) / 2.0);
            }
        }
        return acc;
    }

    private String choosePreferLowWaste(List<String> pool, Set<String> highWaste, Map<String, String> substitute, Random random) {
        List<String> candidates = pool.stream().filter(p -> !highWaste.contains(p)).toList();
        if (!candidates.isEmpty()) return candidates.get(random.nextInt(candidates.size()));
        for (String p : pool) {
            String sub = substitute.get(p);
            if (sub != null && !highWaste.contains(sub)) return sub;
        }
        return pool.get(random.nextInt(pool.size()));
    }
}

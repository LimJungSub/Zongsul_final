package com.zongsul.backend.api;

import com.zongsul.backend.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/today")
    public Map<String, Double> today() {
        return statsService.averagesForDate(LocalDate.now());
    }

    public record DateAverages(LocalDate date, Map<String, Double> averages) {}

    @GetMapping("/history")
    public List<DateAverages> history(@RequestParam(name = "days", defaultValue = "7") int days) {
        List<DateAverages> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            list.add(new DateAverages(d, statsService.averagesForDate(d)));
        }
        return list;
    }
}

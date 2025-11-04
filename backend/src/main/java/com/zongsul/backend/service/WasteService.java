package com.zongsul.backend.service;

import com.zongsul.backend.domain.waste.WasteEntry;
import com.zongsul.backend.domain.waste.WasteEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WasteService
 * - 잔반 데이터 저장/조회 및 간단 추천 계산
 */
@Service
public class WasteService {

    private final WasteEntryRepository repo;

    public WasteService(WasteEntryRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public WasteEntry add(String menu, int remaining, int served, LocalDate date) {
        WasteEntry e = new WasteEntry();
        e.setMenu(menu);
        e.setRemaining(remaining);
        e.setServed(served);
        e.setDate(date);
        return repo.save(e);
    }

    public List<WasteEntry> list(LocalDate from, LocalDate to) {
        return repo.findByDateBetweenOrderByDateAsc(from, to);
    }

    /**
     * 최근 구간의 메뉴별 평균 남김비율(remaining/served) 기준으로 낮은 순 추천
     */
    public List<Map<String, Object>> recommend(LocalDate from, LocalDate to, int limit) {
        List<WasteEntry> entries = list(from, to);
        Map<String, double[]> stats = new HashMap<>(); // menu -> [sumRemaining, sumServed]
        for (WasteEntry e : entries) {
            stats.computeIfAbsent(e.getMenu(), k -> new double[2]);
            stats.get(e.getMenu())[0] += e.getRemaining();
            stats.get(e.getMenu())[1] += Math.max(1, e.getServed());
        }
        return stats.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("menu", e.getKey());
                    m.put("wasteRate", e.getValue()[0] / e.getValue()[1]);
                    return m;
                })
                .sorted(Comparator.comparingDouble(m -> ((Number)m.get("wasteRate")).doubleValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}

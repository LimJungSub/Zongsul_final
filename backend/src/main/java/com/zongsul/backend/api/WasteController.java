package com.zongsul.backend.api;

import com.zongsul.backend.domain.waste.WasteEntry;
import com.zongsul.backend.service.WasteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WasteController {

    private final WasteService service;

    public WasteController(WasteService service) {
        this.service = service;
    }

    public record WasteCreate(String menu, int remaining, int served,
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {}

    @PostMapping("/waste")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WasteEntry> add(@RequestBody WasteCreate req) {
        return ResponseEntity.ok(service.add(req.menu(), req.remaining(), req.served(), req.date()));
    }

    @GetMapping("/waste")
    public List<WasteEntry> list(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(from, to);
    }

    @GetMapping("/recommendations")
    public List<Map<String,Object>> recommend(@RequestParam(defaultValue = "3") int limit,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate toDate = (to == null) ? LocalDate.now() : to;
        LocalDate fromDate = (from == null) ? toDate.minusDays(7) : from;
        return service.recommend(fromDate, toDate, Math.max(1, limit));
    }
}

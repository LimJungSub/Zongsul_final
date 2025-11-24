package com.zongsul.backend.api;

import com.zongsul.backend.domain.distribution.DistributionSession;
import com.zongsul.backend.service.DistributionService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/distribution")
@CrossOrigin(origins = "*")
public class DistributionController {

    private final DistributionService distributionService;

    public DistributionController(DistributionService distributionService) {
        this.distributionService = distributionService;
    }

    // ========================
    // 1) 반찬 등록
    // ========================
    public record StartRequest(@NotBlank String menuName, @Min(1) int capacity) {}
    public record StartResponse(Long sessionId, String menuName, int capacity) {}

    @PostMapping("/start")
    public StartResponse start(@RequestBody StartRequest req) {
        DistributionSession s = distributionService.start(req.menuName(), req.capacity());
        return new StartResponse(s.getId(), s.getMenuName(), s.getCapacity());
    }

    // ========================
    // 2) 활성 세션 조회
    // ========================
    @GetMapping("/active")
    public List<DistributionSession> getActiveSessions() {
        return distributionService.getActiveSessions();
    }

    // ========================
    // 3) 손님 신청(클레임)
    // ========================
    public record ClaimRequest(
            @NotBlank String userName,
            @NotBlank String studentId
    ) {}

    public record ClaimResponse(boolean success, String message, Integer remaining) {}

    @PostMapping("/{sessionId}/claim")
    public ClaimResponse claim(
            @PathVariable Long sessionId,
            @RequestBody ClaimRequest req
    ) {
        var r = distributionService.claim(
                sessionId, req.userName(), req.studentId());
        return new ClaimResponse(r.success(), r.message(), r.remaining());
    }
}

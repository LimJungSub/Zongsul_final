package com.zongsul.backend.api;

import com.zongsul.backend.domain.distribution.DistributionSession;
import com.zongsul.backend.service.DistributionService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distribution")
public class DistributionController {

    private final DistributionService distributionService;

    public DistributionController(DistributionService distributionService) {
        this.distributionService = distributionService;
    }

    public record StartRequest(@NotBlank String menuName, @Min(1) int capacity) {}
    public record StartResponse(Long sessionId, int capacity) {}

    @PostMapping("/start")
    public StartResponse start(@RequestBody StartRequest req) {
        DistributionSession s = distributionService.start(req.menuName(), req.capacity());
        return new StartResponse(s.getId(), s.getCapacity());
    }

    public record ClaimRequest(@NotNull Long sessionId, @NotBlank String userName) {}
    public record ClaimResponse(boolean success, String message, Integer remaining) {}

    @PostMapping("/claim")
    public ClaimResponse claim(@RequestBody ClaimRequest req) {
        var r = distributionService.claim(req.sessionId(), req.userName());
        return new ClaimResponse(r.success(), r.message(), r.remaining());
    }
}

package com.zongsul.backend.service;

import com.zongsul.backend.domain.distribution.DistributionClaim;
import com.zongsul.backend.domain.distribution.DistributionClaimRepository;
import com.zongsul.backend.domain.distribution.DistributionSession;
import com.zongsul.backend.domain.distribution.DistributionSessionRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DistributionService
 * - 배식 시작/수령 기능을 제공합니다.
 * - 세션 차감은 JPA 낙관적 락을 활용하여 동시성 경쟁 상황에서 안전하게 처리합니다.
 */
@Service
public class DistributionService {

    private final DistributionSessionRepository sessionRepo;
    private final DistributionClaimRepository claimRepo;

    public DistributionService(DistributionSessionRepository sessionRepo, DistributionClaimRepository claimRepo) {
        this.sessionRepo = sessionRepo;
        this.claimRepo = claimRepo;
    }

    /**
     * 배식 세션 생성: menuName과 capacity를 받아 세션을 오픈합니다.
     */
    @Transactional
    public DistributionSession start(String menuName, int capacity) {
        DistributionSession s = new DistributionSession(menuName, capacity);
        return sessionRepo.save(s);
    }

    /**
     * 사용자 수령 처리
     * - 이미 수령했는지 확인 -> 남은 수량 차감 -> 0이면 세션 비활성화
     * - 낙관적 락 충돌 시(동시성) Runtime 예외가 날 수 있으므로 상위에서 재시도 전략을 둘 수 있습니다.
     */
    @Transactional
    public ClaimResult claim(Long sessionId, String userName) {
        DistributionSession s = sessionRepo.findById(sessionId).orElse(null);
        if (s == null) return ClaimResult.fail("session not found");
        if (!Boolean.TRUE.equals(s.getActive())) return ClaimResult.fail("closed");
        if (s.getRemainingCount() <= 0) {
            s.setActive(false);
            return ClaimResult.fail("sold out");
        }
        if (claimRepo.existsBySessionIdAndUserName(sessionId, userName)) {
            return ClaimResult.fail("already claimed");
        }

        try {
            s.setRemainingCount(s.getRemainingCount() - 1);
            if (s.getRemainingCount() <= 0) s.setActive(false);
            claimRepo.save(new DistributionClaim(s, userName));
            return ClaimResult.ok(s.getRemainingCount());
        } catch (OptimisticLockingFailureException e) {
            return ClaimResult.fail("concurrent update");
        }
    }

    public record ClaimResult(boolean success, String message, Integer remaining) {
        public static ClaimResult ok(int remaining) { return new ClaimResult(true, "ok", remaining); }
        public static ClaimResult fail(String msg) { return new ClaimResult(false, msg, null); }
    }
}

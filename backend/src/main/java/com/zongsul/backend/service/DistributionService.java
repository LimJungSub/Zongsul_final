package com.zongsul.backend.service;

import com.zongsul.backend.domain.distribution.DistributionClaim;
import com.zongsul.backend.domain.distribution.DistributionClaimRepository;
import com.zongsul.backend.domain.distribution.DistributionSession;
import com.zongsul.backend.domain.distribution.DistributionSessionRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * ✅ 배식 세션 생성 (관리자가 반찬 등록)
     */
    @Transactional
    public DistributionSession start(String menuName, int capacity) {
        System.out.println("💥 start() 호출됨 → menuName=" + menuName + ", capacity=" + capacity);

        sessionRepo.deactivateAllActiveSessions();
        DistributionSession s = new DistributionSession(menuName, capacity);
        return sessionRepo.save(s);
    }

    /**
     * ✅ 사용자 수령 처리
     */
    @Transactional
    public ClaimResult claim(Long sessionId, String userName, String studentId) {

        DistributionSession s = sessionRepo.findWithLockById(sessionId).orElse(null);
        if (s == null) return ClaimResult.fail("session not found");
        if (!Boolean.TRUE.equals(s.getActive())) return ClaimResult.fail("closed");
        if (s.getRemainingCount() <= 0) {
            s.setActive(false);
            return ClaimResult.fail("sold out");
        }

        if (claimRepo.existsBySessionIdAndName(sessionId, userName)) {
            return ClaimResult.fail("already claimed");
        }

        try {
            // 남은 수량 감소
            s.setRemainingCount(s.getRemainingCount() - 1);
            if (s.getRemainingCount() <= 0) s.setActive(false);

            // Claim 저장
            claimRepo.save(new DistributionClaim(s, userName, studentId));

            return ClaimResult.ok(s.getRemainingCount());

        } catch (Exception e) {
            return ClaimResult.fail("concurrent update");
        }
    }


    /**
     * ✅ 현재 배포 중(Active) 세션 목록 조회 — 손님용
     *   (프론트엔드에서 /distribution/active 로 호출)
     */
    @Transactional(readOnly = true)
    public List<DistributionSession> getActiveSessions() {
        return sessionRepo.findAll()
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .toList();
    }

    /**
     * ✅ ClaimResult 내부 레코드
     */
    public record ClaimResult(boolean success, String message, Integer remaining) {
        public static ClaimResult ok(int remaining) {
            return new ClaimResult(true, "ok", remaining);
        }

        public static ClaimResult fail(String msg) {
            return new ClaimResult(false, msg, null);
        }
    }
}

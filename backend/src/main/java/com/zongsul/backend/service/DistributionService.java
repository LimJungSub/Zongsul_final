package com.zongsul.backend.service;

import com.zongsul.backend.domain.distribution.DistributionClaim;
import com.zongsul.backend.domain.distribution.DistributionClaimRepository;
import com.zongsul.backend.domain.distribution.DistributionSession;
import com.zongsul.backend.domain.distribution.DistributionSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DistributionService {

    private final DistributionSessionRepository sessionRepo;
    private final DistributionClaimRepository claimRepo;

    public DistributionService(DistributionSessionRepository sessionRepo,
                               DistributionClaimRepository claimRepo) {
        this.sessionRepo = sessionRepo;
        this.claimRepo = claimRepo;
    }

    @Transactional
    public DistributionSession start(String menuName, int capacity) {
        DistributionSession session = new DistributionSession(menuName, capacity);
        return sessionRepo.save(session);
    }

    @Transactional
    public ClaimResult claim(Long sessionId, String userName, String studentId) {

        DistributionSession s = sessionRepo.findWithLockById(sessionId).orElse(null);
        if (s == null) return ClaimResult.fail("session not found");
        if (!Boolean.TRUE.equals(s.getActive())) return ClaimResult.fail("closed");

        if (claimRepo.existsBySessionIdAndStudentId(sessionId, studentId)) {
            return ClaimResult.fail("already claimed");
        }

        if (s.getRemainingCount() <= 0) {
            s.setActive(false);
            return ClaimResult.fail("sold out");
        }

        try {
            s.setRemainingCount(s.getRemainingCount() - 1);
            if (s.getRemainingCount() <= 0) s.setActive(false);

            claimRepo.save(new DistributionClaim(s, userName, studentId));

            return ClaimResult.ok(s.getRemainingCount());
        } catch (Exception e) {
            return ClaimResult.fail("concurrent update");
        }
    }

    @Transactional
    public ClaimResult cancel(Long sessionId, String studentId) {

        DistributionSession s = sessionRepo.findWithLockById(sessionId).orElse(null);
        if (s == null) return ClaimResult.fail("session not found");

        DistributionClaim claim = claimRepo
                .findBySessionIdAndStudentId(sessionId, studentId)
                .orElse(null);

        if (claim == null) return ClaimResult.fail("claim not found");

        try {
            claimRepo.delete(claim);
            s.setRemainingCount(s.getRemainingCount() + 1);

            if (s.getRemainingCount() > 0) s.setActive(true);

            return ClaimResult.ok(s.getRemainingCount());
        } catch (Exception e) {
            return ClaimResult.fail("concurrent update");
        }
    }

    @Transactional
    public ClaimResult markDone(Long sessionId, String studentId) {

        DistributionSession s = sessionRepo.findWithLockById(sessionId).orElse(null);
        if (s == null) return ClaimResult.fail("session not found");

        DistributionClaim claim = claimRepo
                .findBySessionIdAndStudentId(sessionId, studentId)
                .orElse(null);

        if (claim == null) return ClaimResult.fail("claim not found");

        claim.setDone(true);
        claimRepo.save(claim);

        return ClaimResult.ok(s.getRemainingCount());
    }

    @Transactional(readOnly = true)
    public List<DistributionSession> getActiveSessions() {
        return sessionRepo.findActiveWithClaims();
    }

    public record ClaimResult(boolean success, String message, Integer remaining) {
        public static ClaimResult ok(int remaining) {
            return new ClaimResult(true, "ok", remaining);
        }

        public static ClaimResult fail(String msg) {
            return new ClaimResult(false, msg, null);
        }
    }

    @Transactional
    public DistributionSession saveSession(DistributionSession s) {
        return sessionRepo.save(s);
    }

    @Transactional
    public void deactivateAllActiveSessions() {
        sessionRepo.deactivateAllActiveSessions();
    }
}

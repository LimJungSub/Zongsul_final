package com.zongsul.backend.domain.distribution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistributionClaimRepository extends JpaRepository<DistributionClaim, Long> {
    boolean existsBySessionIdAndStudentId(Long sessionId, String studentId);
    Optional<DistributionClaim> findBySessionIdAndStudentId(Long sessionId, String studentId);
}

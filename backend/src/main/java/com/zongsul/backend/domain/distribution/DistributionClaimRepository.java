package com.zongsul.backend.domain.distribution;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionClaimRepository extends JpaRepository<DistributionClaim, Long> {
    boolean existsBySessionIdAndName(Long sessionId, String name);
}
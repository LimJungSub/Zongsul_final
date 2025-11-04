package com.zongsul.backend.domain.distribution;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DistributionClaimRepository
 * - 사용자(userName)가 이미 수령했는지 확인하는 exists 쿼리를 제공합니다.
 */
public interface DistributionClaimRepository extends JpaRepository<DistributionClaim, Long> {
    boolean existsBySessionIdAndUserName(Long sessionId, String userName);
}

package com.zongsul.backend.domain.distribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface DistributionSessionRepository extends JpaRepository<DistributionSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DistributionSession> findWithLockById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update DistributionSession s set s.active = false where s.active = true")
    void deactivateAllActiveSessions();

    @Query("SELECT DISTINCT s FROM DistributionSession s LEFT JOIN FETCH s.claims WHERE s.active = true")
    List<DistributionSession> findActiveWithClaims();
}

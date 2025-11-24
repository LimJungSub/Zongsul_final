package com.zongsul.backend.domain.distribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;


public interface DistributionSessionRepository extends JpaRepository<DistributionSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DistributionSession> findWithLockById(Long id);
    @Modifying
    @Transactional
    @Query("update DistributionSession s set s.active = false where s.active = true")
    void deactivateAllActiveSessions();
}

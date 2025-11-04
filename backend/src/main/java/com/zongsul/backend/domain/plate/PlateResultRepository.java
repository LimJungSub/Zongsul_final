package com.zongsul.backend.domain.plate;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PlateResultRepository
 * - PlateResult에 대한 CRUD 저장소
 */
public interface PlateResultRepository extends JpaRepository<PlateResult, Long> {
}

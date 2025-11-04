package com.zongsul.backend.domain.waste;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * WasteEntryRepository
 */
public interface WasteEntryRepository extends JpaRepository<WasteEntry, Long> {
    List<WasteEntry> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}

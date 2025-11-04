package com.zongsul.backend.domain.waste;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * WasteEntry 엔티티
 * - 메뉴별 잔반 데이터(남은 수량, 제공 수량, 날짜) 저장.
 */
@Entity
@Table(name = "waste_entry")
@Getter
@Setter
@NoArgsConstructor
public class WasteEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String menu;

    @Column(nullable = false)
    private Integer remaining; // 남은 개수/양

    @Column(nullable = false)
    private Integer served; // 제공 개수/양

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}

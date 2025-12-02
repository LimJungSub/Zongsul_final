package com.zongsul.backend.domain.distribution;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "distribution_claim",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_claim_session_student",
                columnNames = {"session_id", "student_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DistributionClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DistributionSession session;

    @Column(name = "user_name", nullable = false, length = 100)
    private String name;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @Column(name = "done", nullable = false)
    private boolean done = false;

    public DistributionClaim(DistributionSession session, String name, String studentId) {
        this.session = session;
        this.name = name;
        this.studentId = studentId;
        this.claimedAt = LocalDateTime.now();
    }

    public DistributionClaim(DistributionSession session, String name) {
        this.session = session;
        this.name = name;
        this.studentId = "UNKNOWN";
        this.claimedAt = LocalDateTime.now();
    }

    public String getUserName() { return this.name; }
    public void setUserName(String userName) { this.name = userName; }
}

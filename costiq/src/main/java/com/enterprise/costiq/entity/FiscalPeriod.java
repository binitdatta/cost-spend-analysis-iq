package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "fiscal_periods")
@Getter @Setter @NoArgsConstructor @ToString
public class FiscalPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fiscal_year", nullable = false)
    private int fiscalYear;

    @Column(nullable = false)
    private int quarter;

    @Column(name = "period_name", nullable = false, length = 20)
    private String periodName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_closed", nullable = false)
    private boolean closed;

    public String getDisplayName() {
        return periodName + (closed ? " [Closed]" : " [Open]");
    }
}

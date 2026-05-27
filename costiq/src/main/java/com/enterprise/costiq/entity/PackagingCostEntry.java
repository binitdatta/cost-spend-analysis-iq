package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "packaging_cost_entries")
@Getter @Setter @NoArgsConstructor @ToString
public class PackagingCostEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packaging_item_id", nullable = false)
    private PackagingItem packagingItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id", nullable = false)
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_period_id", nullable = false)
    private FiscalPeriod fiscalPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "unit_cost_usd", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitCostUsd;

    @Column(name = "total_cost_usd", precision = 18, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalCostUsd;

    @Column(name = "invoice_ref", length = 50)
    private String invoiceRef;

    @Column(name = "po_number", length = 50)
    private String poNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal computedTotal() {
        if (quantity != null && unitCostUsd != null) {
            return BigDecimal.valueOf(quantity).multiply(unitCostUsd);
        }
        return BigDecimal.ZERO;
    }
}

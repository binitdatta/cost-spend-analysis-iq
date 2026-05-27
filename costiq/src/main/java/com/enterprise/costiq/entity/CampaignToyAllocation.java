package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_toy_allocations")
@Getter @Setter @NoArgsConstructor @ToString
public class CampaignToyAllocation {

    public enum DistributionChannel { RETAIL, DIRECT, ONLINE, POPUP }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toy_item_id", nullable = false)
    private ToyItem toyItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_period_id", nullable = false)
    private FiscalPeriod fiscalPeriod;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "unit_cost_usd", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitCostUsd;

    @Column(name = "total_cost_usd", precision = 18, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalCostUsd;

    @Enumerated(EnumType.STRING)
    @Column(name = "distribution_channel", nullable = false, length = 10)
    private DistributionChannel distributionChannel = DistributionChannel.RETAIL;

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

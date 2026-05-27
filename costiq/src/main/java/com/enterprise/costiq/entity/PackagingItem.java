package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packaging_items")
@Getter @Setter @NoArgsConstructor @ToString
public class PackagingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packaging_type_id", nullable = false)
    private PackagingType packagingType;

    @Column(name = "dimensions_cm", length = 50)
    private String dimensionsCm;

    @Column(name = "weight_grams", precision = 8, scale = 2)
    private BigDecimal weightGrams;

    @Column(name = "base_cost_usd", nullable = false, precision = 12, scale = 4)
    private BigDecimal baseCostUsd;

    @Column(name = "min_order_qty", nullable = false)
    private int minOrderQty = 1000;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

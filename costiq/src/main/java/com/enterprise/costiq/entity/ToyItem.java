package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "toy_items")
@Getter @Setter @NoArgsConstructor @ToString
public class ToyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toy_category_id", nullable = false)
    private ToyCategory toyCategory;

    @Column(name = "licensed_ip", length = 100)
    private String licensedIp;

    @Column(length = 50)
    private String material;

    @Column(name = "safety_certified", nullable = false)
    private boolean safetyCertified;

    @Column(name = "unit_cost_usd", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitCostUsd;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

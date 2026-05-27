package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packaging_types")
@Getter @Setter @NoArgsConstructor @ToString
public class PackagingType {

    public enum Material { PAPER, PLASTIC, CARDBOARD, FOIL, BIODEGRADABLE, GLASS, METAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Material material;

    @Column(name = "is_recyclable", nullable = false)
    private boolean recyclable;
}

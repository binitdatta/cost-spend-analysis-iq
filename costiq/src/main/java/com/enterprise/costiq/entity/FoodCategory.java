package com.enterprise.costiq.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_categories")
@Getter @Setter @NoArgsConstructor @ToString
public class FoodCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}

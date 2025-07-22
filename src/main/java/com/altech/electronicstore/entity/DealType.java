package com.altech.electronicstore.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "deal_type")
@Data
public class DealType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "strategy_class")
    private String strategyClass;
}

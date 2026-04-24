package com.example.webbanhang.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer categoryId;

    @Column(name = "CategoryName", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "Description", length = 255)
    private String description;

    // ── Relationships ────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}

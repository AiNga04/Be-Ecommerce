package com.zyna.dev.ecommerce.products.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "colors")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Stores hex code or other color code
    @Column(nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;
}

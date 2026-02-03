package com.erp.enterprise.entity.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Category Entity
 *
 * Business Logic:
 * - Hierarchical categorization of products
 * - Parent-child relationships for sub-categories
 *
 * Examples:
 * - Electronics (parent)
 *   - Laptops (child)
 *   - Mobile Phones (child)
 * - Office Supplies (parent)
 *   - Stationery (child)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Parent category for hierarchical structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category parentCategory;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
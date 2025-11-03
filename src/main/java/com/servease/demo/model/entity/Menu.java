package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE menus SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "available", nullable = false)
    private boolean available;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateMenu(
            String name,
            Integer price,
            Category category,
            boolean available
    ) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
    }
}

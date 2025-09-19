package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="stores")
@Getter
@NoArgsConstructor
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Builder
    public Store(User owner, String name) {
        this.owner = owner;
        this.name = name;
    }
}

package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="stores")
@Getter
@ToString(exclude = {"owner", "restaurantTables"}) // 순환 참조 방지를 위해 연관관계 필드 제외
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantTable> restaurantTables = new ArrayList<>();

    @Builder
    private Store(User owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public static Store create(User owner, String name) {
        Store store = Store.builder()
                .owner(owner)
                .name(name)
                .build();

        if (owner != null && !owner.getStores().contains(store)) {
            owner.getStores().add(store);
        }
        return store;
    }
}

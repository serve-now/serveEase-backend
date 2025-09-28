package com.servease.demo.model.entity;

import com.servease.demo.model.enums.RestaurantTableStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "restaurant_tables")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_table_number", nullable = false)
    private Integer tableNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "store_id")
    private Store store;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RestaurantTableStatus status;

    @OneToMany(mappedBy = "restaurantTable", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();


    public void updateStatus(RestaurantTableStatus newStatus) {
        this.status = newStatus;
    }

}

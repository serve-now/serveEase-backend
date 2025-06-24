package com.servease.demo.model.entity;

import com.servease.demo.model.enums.RestaurantTableStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name="restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="restaurant_table_number", nullable = false, unique = true, length = 10)
    private Integer tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private RestaurantTableStatus status;


}

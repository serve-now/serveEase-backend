package com.servease.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.servease.demo.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.boot.model.naming.IllegalIdentifierException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable restaurantTable;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = false;

    @CreationTimestamp
    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();


    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        this.orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public void calculateTotalPrice() {
        this.totalPrice = this.orderItems.stream()
                .mapToInt(item -> item.getQuantity() * item.getItemPrice())
                .sum();
    }

    public void removeItemById(Long orderItemId){
        if (this.status == OrderStatus.COMPLETED || this.status == OrderStatus.CANCELED) {
            throw new IllegalStateException("Cannot remove items from a completed or canceled order.");
        }

        OrderItem itemToRemove = this.orderItems.stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OrderItem with ID " +orderItemId +" not found in this order."));

        this.removeOrderItem(itemToRemove);
        this.calculateTotalPrice();
    }
}

package com.carrental.carrent.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE cars SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private CarType carType;
    @Column(nullable = false)
    private int inventory;
    @Column(nullable = false)
    private BigDecimal dailyFee;
    @Column(nullable = false)
    private boolean isDeleted = false;
}


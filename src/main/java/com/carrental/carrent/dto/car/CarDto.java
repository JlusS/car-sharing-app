package com.carrental.carrent.dto.car;

import com.carrental.carrent.model.CarType;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private CarType carType;
    private int inventory;
    private BigDecimal dailyFee;
}

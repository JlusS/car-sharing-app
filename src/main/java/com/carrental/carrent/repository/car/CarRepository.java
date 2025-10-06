package com.carrental.carrent.repository.car;

import com.carrental.carrent.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}

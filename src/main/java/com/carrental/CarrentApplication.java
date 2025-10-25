package com.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarrentApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarrentApplication.class, args);
    }

}

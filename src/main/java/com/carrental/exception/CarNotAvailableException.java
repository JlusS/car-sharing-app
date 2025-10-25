package com.carrental.exception;

public class CarNotAvailableException extends RuntimeException {
    public CarNotAvailableException(String message) {
        super(message);
    }

    public CarNotAvailableException(Long carId) {
        super("Car with id " + carId + " is not available for rental");
    }
}

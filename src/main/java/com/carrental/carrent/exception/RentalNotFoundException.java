package com.carrental.carrent.exception;

public class RentalNotFoundException extends RuntimeException {
    public RentalNotFoundException(String message) {
        super(message);
    }

    public RentalNotFoundException(Long rentalId) {
        super("Rental not found with id: " + rentalId);
    }
}

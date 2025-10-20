package com.carrental.carrent.repository.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carrental.carrent.model.Car;
import com.carrental.carrent.model.CarType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Save car - should save and return car with generated ID")
    void save_ValidCar_ShouldSaveCar() {
        // Given
        Car car = new Car();
        car.setModel("Camry");
        car.setBrand("Toyota");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));

        // When
        Car savedCar = carRepository.save(car);

        // Then
        assertNotNull(savedCar);
        assertNotNull(savedCar.getId());
        assertEquals("Camry", savedCar.getModel());
        assertEquals("Toyota", savedCar.getBrand());
        assertEquals(CarType.SEDAN, savedCar.getCarType());
        assertEquals(5, savedCar.getInventory());
        assertEquals(BigDecimal.valueOf(50.00), savedCar.getDailyFee());
        assertFalse(savedCar.isDeleted());
    }

    @Test
    @DisplayName("Find car by ID - should return car when exists")
    void findById_ExistingId_ShouldReturnCar() {
        // Given
        Car car = new Car();
        car.setModel("Civic");
        car.setBrand("Honda");
        car.setCarType(CarType.SEDAN);
        car.setInventory(3);
        car.setDailyFee(BigDecimal.valueOf(40.00));
        Car savedCar = carRepository.save(car);

        // When
        Optional<Car> foundCar = carRepository.findById(savedCar.getId());

        // Then
        assertTrue(foundCar.isPresent());
        assertEquals(savedCar.getId(), foundCar.get().getId());
        assertEquals("Civic", foundCar.get().getModel());
        assertEquals("Honda", foundCar.get().getBrand());
    }

    @Test
    @DisplayName("Find car by ID - should return empty when not exists")
    void findById_NonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Car> foundCar = carRepository.findById(999L);

        // Then
        assertTrue(foundCar.isEmpty());
    }

    @Test
    @DisplayName("Find all cars - should return all saved cars")
    void findAll_WithSavedCars_ShouldReturnAllCars() {
        // Given
        Car car1 = new Car();
        car1.setModel("Camry");
        car1.setBrand("Toyota");
        car1.setCarType(CarType.SEDAN);
        car1.setInventory(5);
        car1.setDailyFee(BigDecimal.valueOf(50.00));

        Car car2 = new Car();
        car2.setModel("X5");
        car2.setBrand("BMW");
        car2.setCarType(CarType.SUV);
        car2.setInventory(2);
        car2.setDailyFee(BigDecimal.valueOf(100.00));

        carRepository.save(car1);
        carRepository.save(car2);

        // When
        List<Car> allCars = carRepository.findAll();

        // Then
        assertEquals(2, allCars.size());
    }

    @Test
    @DisplayName("Update car - should update car details")
    void updateCar_ExistingCar_ShouldUpdateDetails() {
        // Given
        Car car = new Car();
        car.setModel("Camry");
        car.setBrand("Toyota");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));
        Car savedCar = carRepository.save(car);

        // When
        savedCar.setModel("Camry Hybrid");
        savedCar.setDailyFee(BigDecimal.valueOf(55.00));
        savedCar.setInventory(3);
        Car updatedCar = carRepository.save(savedCar);

        // Then
        assertEquals(savedCar.getId(), updatedCar.getId());
        assertEquals("Camry Hybrid", updatedCar.getModel());
        assertEquals(BigDecimal.valueOf(55.00), updatedCar.getDailyFee());
        assertEquals(3, updatedCar.getInventory());
    }

    @Test
    @DisplayName("Delete car by ID - should remove car from database")
    void deleteById_ExistingCar_ShouldDeleteCar() {
        // Given
        Car car = new Car();
        car.setModel("Camry");
        car.setBrand("Toyota");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));
        Car savedCar = carRepository.save(car);
        long initialCount = carRepository.count();

        // When
        carRepository.deleteById(savedCar.getId());

        // Then
        assertEquals(initialCount - 1, carRepository.count());
        assertTrue(carRepository.findById(savedCar.getId()).isEmpty());
    }

    @Test
    @DisplayName("Soft delete car - should mark car as deleted")
    void softDelete_ExistingCar_ShouldMarkAsDeleted() {
        // Given
        Car car = new Car();
        car.setModel("Camry");
        car.setBrand("Toyota");
        car.setCarType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50.00));
        Car savedCar = carRepository.save(car);

        // When
        savedCar.setDeleted(true);
        Car updatedCar = carRepository.save(savedCar);

        // Then
        assertTrue(updatedCar.isDeleted());
    }

    @Test
    @DisplayName("Find all cars with different types - should return cars with correct types")
    void findAll_WithDifferentCarTypes_ShouldReturnCorrectTypes() {
        // Given
        Car sedan = new Car();
        sedan.setModel("Camry");
        sedan.setBrand("Toyota");
        sedan.setCarType(CarType.SEDAN);
        sedan.setInventory(5);
        sedan.setDailyFee(BigDecimal.valueOf(50.00));

        Car suv = new Car();
        suv.setModel("X5");
        suv.setBrand("BMW");
        suv.setCarType(CarType.SUV);
        suv.setInventory(2);
        suv.setDailyFee(BigDecimal.valueOf(100.00));

        Car hatchback = new Car();
        hatchback.setModel("Golf");
        hatchback.setBrand("Volkswagen");
        hatchback.setCarType(CarType.HATCHBACK);
        hatchback.setInventory(4);
        hatchback.setDailyFee(BigDecimal.valueOf(45.00));

        carRepository.save(sedan);
        carRepository.save(suv);
        carRepository.save(hatchback);

        // When
        List<Car> allCars = carRepository.findAll();

        // Then
        assertEquals(3, allCars.size());
        assertTrue(allCars.stream().anyMatch(c -> c.getCarType() == CarType.SEDAN));
        assertTrue(allCars.stream().anyMatch(c -> c.getCarType() == CarType.SUV));
        assertTrue(allCars.stream().anyMatch(c -> c.getCarType() == CarType.HATCHBACK));
    }
}

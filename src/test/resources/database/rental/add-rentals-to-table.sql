DELETE FROM rentals;
DELETE FROM cars;
DELETE FROM users;

INSERT INTO cars (id, brand, model, car_type, inventory, daily_fee, is_deleted) VALUES
(1, 'Toyota', 'Camry', 0, 5, 50.00, false),
(2, 'Honda', 'Civic', 0, 3, 45.00, false),
(3, 'BMW', 'X5', 1, 2, 100.00, false);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id) VALUES
(1, '2024-10-18', '2024-10-25', NULL, 1, 1),
(2, '2024-10-20', '2024-10-27', NULL, 2, 1),
(3, '2024-10-15', '2024-10-22', '2024-10-22', 3, 4);

INSERT INTO users (id, email, password, first_name, last_name, role, is_deleted) VALUES
(1, 'customer@test.com', '$2a$10$exampleHashedPassword1', 'John', 'Doe', 1, false),
(2, 'manager@test.com', '$2a$10$exampleHashedPassword2', 'Jane', 'Smith', 0, false),
(3, 'admin@test.com', '$2a$10$exampleHashedPassword3', 'Admin', 'User', 0, false);
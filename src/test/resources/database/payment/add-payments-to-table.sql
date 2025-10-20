INSERT INTO users (id, email, first_name, last_name, password, role, is_deleted)
VALUES
(1, 'customer@example.com', 'John', 'Doe', '$2a$10$exampleEncodedPassword', 1, false),
(2, 'manager@example.com', 'Jane', 'Smith', '$2a$10$exampleEncodedPassword', 0, false),
(3, 'alice@example.com', 'Alice', 'Johnson', '$2a$10$exampleEncodedPassword', 1, false);

INSERT INTO cars (id, model, brand, car_type, inventory, daily_fee, is_deleted)
VALUES
(1, 'Model S', 'Tesla', 0, 5, 100.00, false),
(2, 'X5', 'BMW', 1, 3, 150.00, false),
(3, 'Golf', 'Volkswagen', 2, 7, 80.00, false),
(4, 'V90', 'Volvo', 3, 2, 120.00, false);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES
(1, '2025-10-16', '2025-10-30', NULL, 1, 1),
(2, '2025-10-15', '2025-10-18', '2025-10-17', 2, 2),
(3, '2025-10-14', '2025-10-19', NULL, 3, 3),
(4, '2025-10-13', '2025-10-16', NULL, 4, 1);

INSERT INTO payments (id, status, payment_type, rental_id, session_url, session_id, amount_to_pay, is_deleted) VALUES
(1, 'PENDING', 'PAYMENT', 1, 'https://stripe.com/session/test_session_123', 'test_session_123', 400.00, false);
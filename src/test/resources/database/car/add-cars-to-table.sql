INSERT INTO cars (id, model, brand, car_type, inventory, daily_fee, is_deleted) VALUES (1, 'Camry', 'Toyota', 0, 5, 50.00, false);
INSERT INTO cars (id, model, brand, car_type, inventory, daily_fee, is_deleted) VALUES (2, 'Civic', 'Honda', 0, 3, 45.00, false);
INSERT INTO cars (id, model, brand, car_type, inventory, daily_fee, is_deleted) VALUES (3, 'Model 3', 'Tesla', 0, 2, 80.00, false);
INSERT INTO cars (id, model, brand, car_type, inventory, daily_fee, is_deleted) VALUES (4, 'RAV4', 'Toyota', 1, 4, 65.00, false);
INSERT INTO cars (id, model, brand, car_type, inventory, daily_fee, is_deleted) VALUES (5, 'Golf', 'Volkswagen', 2, 6, 40.00, false);

ALTER TABLE cars ALTER COLUMN id RESTART WITH 6;
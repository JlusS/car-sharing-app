INSERT INTO users (id, email, password, first_name, last_name, role, is_deleted) VALUES
(1, 'customer@test.com', '$2a$10$exampleHashedPassword1', 'John', 'Doe', 1, false),
(2, 'manager@test.com', '$2a$10$exampleHashedPassword2', 'Jane', 'Smith', 0, false),
(3, 'admin@test.com', '$2a$10$exampleHashedPassword3', 'Admin', 'User', 0, false);
-- Use the correct database
USE sys;

-- Reset table (safe during development)
DROP TABLE IF EXISTS subscriber;

-- Create subscriber table
CREATE TABLE subscriber (
    sub_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    PRIMARY KEY (sub_id)
);

-- Insert sample data (password_hash values are placeholders)
INSERT INTO subscriber (username, email, phone, password_hash) VALUES
('alice', 'alice@mail.com', '0501234567', '$2a$10$alicehash'),
('bob', 'bob@mail.com', '0529876543', '$2a$10$bobhash'),
('charlie', 'charlie@mail.com', '0541112233', '$2a$10$charliehash');

-- Verify
SELECT * FROM subscriber;

-- Make sure we use the correct database
USE sys;

-- Drop table if it exists (development-safe)
DROP TABLE IF EXISTS subscriber;

-- Create subscriber table
CREATE TABLE subscriber (
    sub_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    PRIMARY KEY (sub_id)
);

-- Insert sample subscribers
-- NOTE: password_hash values are dummy placeholders
INSERT INTO subscriber
(username, first_name, last_name, email, phone, password_hash)
VALUES
('tal123', 'Tal', 'Levi', 'tal@mail.com', '0501234567', '$2a$10$talhash'),
('noam77', 'Noam', 'Cohen', 'noam@mail.com', '0529876543', '$2a$10$noamhash'),
('dana_k', 'Dana', 'Katz', 'dana@mail.com', '0541112233', '$2a$10$danahash'),
('amir90', 'Amir', 'Ben-David', 'amir@mail.com', '0532223344', '$2a$10$amirhash');

-- Verify contents (password is still stored, but selectable)
SELECT * FROM subscriber;

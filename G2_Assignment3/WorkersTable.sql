-- Make sure we use the correct database
USE sys;

-- Drop table if it exists (development-safe)
DROP TABLE IF EXISTS worker;

-- Create worker table
CREATE TABLE workers (
    worker_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
	worker_type ENUM('employee', 'manager') NOT NULL,
    PRIMARY KEY (worker_id)
);

-- Insert sample workers
-- NOTE: password_hash values are dummy placeholders
INSERT INTO workers
(worker_id, username, password_hash, worker_type)
VALUES
(15357,'lior22', '$2a$10$liorhash', 'manager'),
(86096,'yael99', '$2a$10$yaelhash', 'employee'),
(90865,'omer_x', '$2a$10$omerhash', 'employee'),
(74839,'shira7', '$2a$10$shirahash', 'employee');


-- Verify contents (password is still stored, but selectable)
SELECT * FROM workers;

USE sys;

-- Delete table if it already exists
DROP TABLE IF EXISTS `order`;

-- Create the table
CREATE TABLE `order` (
    order_number INT NOT NULL,
    order_date DATE NOT NULL,
    number_of_guests INT NOT NULL,
    confirmation_code INT NOT NULL,
    subscriber_id INT NOT NULL DEFAULT 0,
    phone_number INT NOT NULL,
    date_of_placing_order DATE NOT NULL,
    PRIMARY KEY (order_number)
);

-- Insert random example data
INSERT INTO `order` (
    order_number,
    order_date,
    number_of_guests,
    confirmation_code,
    subscriber_id,
    phone_number,
    date_of_placing_order
) VALUES
(1001, '2025-01-10', 2, 83421, 0, 501234567, '2025-01-05'),
(1002, '2025-01-11', 4, 19234, 17, 502345678, '2025-01-06'),
(1003, '2025-01-12', 1, 77654, 0, 503456789, '2025-01-07'),
(1004, '2025-01-13', 6, 44512, 32, 504567890, '2025-01-08'),
(1005, '2025-01-14', 3, 99873, 0, 505678901, '2025-01-09');
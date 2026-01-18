CREATE DATABASE  IF NOT EXISTS `bistro` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `bistro`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bistro
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `res_id` int NOT NULL AUTO_INCREMENT,
  `confirmation_code` VARCHAR(6) DEFAULT NULL,
  `phone` varchar(10) NOT NULL,
  `email` varchar(100) NOT NULL,
  `sub_id` int NOT NULL,
  `start_time` time NOT NULL,
  `finish_time` time NOT NULL,
  `order_date` date NOT NULL,
  `order_status` enum('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'ACCEPTED', 'LATE_CANCEL') DEFAULT NULL,
  `reminded` boolean NOT NULL DEFAULT false,
  `num_diners` int NOT NULL,
  `date_of_placing_order` date NOT NULL,
  `waitlist_enter_time` time NULL,
  PRIMARY KEY (`res_id`),
  KEY `sub_id` (`sub_id`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`sub_id`) REFERENCES `subscriber` (`sub_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DELIMITER //
CREATE TRIGGER before_insert_reservations
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
    DECLARE current_max_id INT;
    SELECT MAX(res_id) INTO current_max_id FROM reservations;
    IF current_max_id IS NULL THEN
        SET NEW.confirmation_code = LPAD(5 % 1000000, 6, '0');
    ELSE
        SET NEW.confirmation_code = LPAD((current_max_id + 1) % 1000000, 6, '0');
    END IF;
END;
//
DELIMITER ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` 
(`phone`, `email`, `sub_id`, `start_time`, `finish_time`, `order_date`, `order_status`, `num_diners`, `date_of_placing_order`, `waitlist_enter_time`) 
VALUES 
('0500000000', 'null@mail.com', 0, '00:00:00', '00:00:00', '2001-01-01', 'COMPLETED', 0, '2001-01-01', NULL),
('0501234567', 'tal@mail.com', 1, '12:30:00', '14:30:00', '2025-01-05', 'CONFIRMED', 2, '2024-12-30', NULL),
('0549705492', 'ofir@mail.com', 10, '14:30:00', '16:30:00', '2025-01-05', 'CONFIRMED', 4, '2024-12-30', NULL),
('0540540545', 'oshri@mail.com', 0, '10:30:00', '12:30:00', '2025-01-05', 'CONFIRMED', 4, '2024-12-30', NULL),
('0529876543', 'noam@mail.com', 2, '18:00:00', '20:00:00', '2025-01-06', 'CONFIRMED', 2, '2024-12-31', NULL),
('0526943286', 'test@mail.com', 5, '18:00:00', '20:00:00', '2025-01-06', 'CONFIRMED', 2, '2024-12-31', NULL),
('0501234567','tal@mail.com',        1,'12:00:00','14:00:00','2025-10-03','COMPLETED', 2,'2025-09-28',NULL),          -- on-time
('0529876543','noam@mail.com',       2,'18:30:00','20:30:00','2025-10-06','ACCEPTED',  4,'2025-10-01','18:00:00'),     -- waitlist
('0541112233','dana@mail.com',       3,'13:10:00','15:10:00','2025-10-09','COMPLETED', 2,'2025-10-05',NULL),          -- late (<15)
('0532223344','amir@mail.com',       4,'20:00:00','22:00:00','2025-10-12','CANCELLED', 6,'2025-10-10',NULL),          -- cancelled
('0526948432','test@mail.com',       5,'19:30:00','21:30:00','2025-10-15','COMPLETED', 2,'2025-10-12','19:00:00'),     -- waitlist
('0500000000','guest@mail.com',      0,'11:30:00','13:30:00','2025-10-18','ACCEPTED',  2,'2025-10-17',NULL),          -- guest, on-time
('0541112233','dana@mail.com',       3,'17:14:00','19:14:00','2025-10-22','ACCEPTED',  3,'2025-10-20',NULL),          -- late (<15)
('0532223344','amir@mail.com',       4,'21:00:00','23:00:00','2025-10-27','COMPLETED', 4,'2025-10-24','20:30:00'),     -- waitlist
('0500000000','guest1@mail.com', 0,'10:00:00','11:30:00','2025-10-02','COMPLETED', 2,'2025-10-01',NULL),
('0500000000','guest2@mail.com', 0,'12:30:00','14:15:00','2025-10-04','ACCEPTED',  3,'2025-10-03','11:30:00'), -- waitlist 60
('0501234567','tal@mail.com',    1,'18:05:00','20:05:00','2025-10-05','COMPLETED', 2,'2025-10-03',NULL),         -- late (<15) => +2h
('0529876543','noam@mail.com',   2,'19:30:00','21:30:00','2025-10-07','ACCEPTED',  4,'2025-10-06','18:30:00'),    -- waitlist 60
('0541112233','dana@mail.com',   3,'13:00:00','14:45:00','2025-10-08','COMPLETED', 5,'2025-10-07',NULL),
('0532223344','amir@mail.com',   4,'20:12:00','21:12:00','2025-10-10','LATE_CANCEL',2,'2025-10-08',NULL),         -- late +2h, late_cancel
('0526948432','test@mail.com',   5,'21:00:00','23:00:00','2025-10-11','CANCELLED',  2,'2025-10-10',NULL),
('0500000000','guest3@mail.com', 0,'17:30:00','19:00:00','2025-10-13','COMPLETED', 2,'2025-10-13','16:30:00'),    -- waitlist 60
('0500000000','guest4@mail.com', 0,'18:30:00','20:00:00','2025-10-14','ACCEPTED',  2,'2025-10-14','18:00:00'),    -- waitlist 30
('0529876543','noam@mail.com',   2,'12:10:00','13:10:00','2025-10-16','COMPLETED', 3,'2025-10-14',NULL),          -- late +2h
('0541112233','dana@mail.com',   3,'14:30:00','16:00:00','2025-10-17','ACCEPTED',  2,'2025-10-16','13:30:00'),    -- waitlist 60
('0500000000','guest5@mail.com', 0,'21:30:00','23:20:00','2025-10-19','COMPLETED', 4,'2025-10-18',NULL),
('0532223344','amir@mail.com',   4,'11:30:00','13:10:00','2025-10-20','COMPLETED', 2,'2025-10-18','10:30:00'),    -- waitlist 60
('0500000000','guest6@mail.com', 0,'19:14:00','21:00:00','2025-10-21','LATE_CANCEL',2,'2025-10-21',NULL),         -- late +2h, late_cancel
('0501234567','tal@mail.com',    1,'20:30:00','22:00:00','2025-10-23','ACCEPTED',  2,'2025-10-22','19:30:00'),    -- waitlist 60
('0500000000','guest17@mail.com',0,'10:30:00','12:00:00','2025-10-01','COMPLETED',2,'2025-09-30',NULL),
('0526948432','test@mail.com',  5,'13:30:00','15:00:00','2025-10-15','ACCEPTED', 2,'2025-10-14','12:30:00'), -- waitlist 60
('0500000000','guest18@mail.com',0,'16:30:00','18:20:00','2025-10-24','COMPLETED',4,'2025-10-23',NULL),
('0541112233','dana@mail.com',  3,'18:30:00','20:00:00','2025-10-25','ACCEPTED', 2,'2025-10-24','17:00:00'), -- waitlist 90
('0500000000','guest19@mail.com',0,'12:00:00','13:30:00','2025-10-26','COMPLETED',5,'2025-10-25',NULL),
('0501234567','tal@mail.com',   1,'19:30:00','21:10:00','2025-10-28','COMPLETED',2,'2025-10-27','18:30:00'), -- waitlist 60
('0500000000','guest20@mail.com',0,'20:12:00','22:00:00','2025-10-29','LATE_CANCEL',2,'2025-10-29',NULL),      -- late +2h
('0529876543','noam@mail.com',  2,'21:30:00','23:30:00','2025-10-30','CANCELLED',2,'2025-10-29',NULL),
('0500000000','guest31@mail.com',0,'11:00:00','12:40:00','2025-10-03','ACCEPTED',2,'2025-10-02','10:00:00'), -- same day had on-time => add waitlist
('0529876543','noam@mail.com', 2,'10:30:00','12:00:00','2025-10-09','COMPLETED',2,'2025-10-08',NULL),         -- day missing
('0500000000','guest32@mail.com',0,'15:12:00','17:12:00','2025-10-09','LATE_CANCEL',3,'2025-10-09',NULL),      -- same day add late_cancel
('0500000000','guest33@mail.com',0,'18:00:00','19:30:00','2025-10-11','ACCEPTED',2,'2025-10-10','17:00:00'),   -- same day existed cancelled => add waitlist/on-time
('0501234567','tal@mail.com',  1,'20:30:00','22:00:00','2025-10-12','COMPLETED',2,'2025-10-11',NULL),          -- day had cancelled => add completed
('0500000000','guest34@mail.com',0,'12:30:00','14:10:00','2025-10-15','ACCEPTED',4,'2025-10-14','11:30:00'),   -- same day existed => add different wait
('0526948432','test@mail.com', 5,'19:00:00','20:30:00','2025-10-16','COMPLETED',2,'2025-10-15',NULL),          -- same day had late => add on-time
('0500000000','guest35@mail.com',0,'10:00:00','12:00:00','2025-10-17','COMPLETED',6,'2025-10-16',NULL),        -- same day existed waitlist => add no-wait
('0500000000','guest36@mail.com',0,'13:30:00','15:00:00','2025-10-22','COMPLETED',3,'2025-10-21',NULL),        -- day existed late => add on-time
('0532223344','amir@mail.com', 4,'18:12:00','20:12:00','2025-10-22','COMPLETED',2,'2025-10-22',NULL),          -- same day add late (+2h)
('0500000000','guest37@mail.com',0,'16:30:00','18:00:00','2025-10-27','ACCEPTED',2,'2025-10-26','15:30:00'),   -- same day existed => add waitlist
('0529876543','noam@mail.com', 2,'21:00:00','23:00:00','2025-10-31','LATE_CANCEL',2,'2025-10-31','20:00:00'),  -- missing day + late_cancel + waitlist 60
/* -------------------- NOV 2025 (11 orders) -------------------- */
('0501234567','tal@mail.com',        1,'12:30:00','14:30:00','2025-11-02','COMPLETED', 2,'2025-10-30',NULL),          -- on-time
('0529876543','noam@mail.com',       2,'18:07:00','20:00:00','2025-11-04','ACCEPTED',  2,'2025-11-01',NULL),          -- late (<15)
('0541112233','dana@mail.com',       3,'19:00:00','21:00:00','2025-11-06','CANCELLED', 5,'2025-11-03',NULL),          -- cancelled
('0532223344','amir@mail.com',       4,'13:00:00','15:00:00','2025-11-08','COMPLETED', 4,'2025-11-06','12:30:00'),     -- waitlist
('0526948432','test@mail.com',       5,'20:30:00','22:30:00','2025-11-10','ACCEPTED',  2,'2025-11-09',NULL),          -- on-time
('0500000000','guest@mail.com',      0,'11:05:00','13:00:00','2025-11-11','COMPLETED', 2,'2025-11-11',NULL),          -- guest, late (<15)
('0501234567','tal@mail.com',        1,'18:30:00','20:30:00','2025-11-13','COMPLETED', 3,'2025-11-10','18:00:00'),     -- waitlist
('0529876543','noam@mail.com',       2,'21:30:00','23:30:00','2025-11-15','ACCEPTED',  2,'2025-11-14','21:00:00'),     -- waitlist
('0541112233','dana@mail.com',       3,'12:00:00','14:00:00','2025-11-18','CANCELLED', 2,'2025-11-16',NULL),          -- cancelled
('0532223344','amir@mail.com',       4,'19:12:00','21:00:00','2025-11-20','COMPLETED', 6,'2025-11-18',NULL),          -- late (<15)
('0526948432','test@mail.com',       5,'18:30:00','20:30:00','2025-11-26','ACCEPTED',  4,'2025-11-22','17:00:00'),     -- waitlist
('0500000000','guest7@mail.com', 0,'10:30:00','12:00:00','2025-11-01','COMPLETED', 2,'2025-10-31',NULL),
('0501234567','tal@mail.com',    1,'12:00:00','13:30:00','2025-11-03','ACCEPTED',  3,'2025-11-02','11:00:00'),    -- waitlist 60
('0529876543','noam@mail.com',   2,'18:14:00','20:00:00','2025-11-05','COMPLETED', 2,'2025-11-03',NULL),          -- late +2h
('0500000000','guest8@mail.com', 0,'20:00:00','22:00:00','2025-11-07','COMPLETED', 6,'2025-11-06',NULL),
('0541112233','dana@mail.com',   3,'21:30:00','23:00:00','2025-11-09','ACCEPTED',  2,'2025-11-08','20:00:00'),    -- waitlist 90
('0532223344','amir@mail.com',   4,'13:30:00','15:00:00','2025-11-12','CANCELLED', 4,'2025-11-10',NULL),
('0526948432','test@mail.com',   5,'19:30:00','21:00:00','2025-11-14','COMPLETED', 2,'2025-11-13','19:00:00'),    -- waitlist 30
('0500000000','guest9@mail.com', 0,'18:30:00','20:00:00','2025-11-16','ACCEPTED',  2,'2025-11-16','17:30:00'),    -- waitlist 60
('0501234567','tal@mail.com',    1,'11:05:00','13:00:00','2025-11-17','LATE_CANCEL',2,'2025-11-17',NULL),          -- late +2h, late_cancel
('0529876543','noam@mail.com',   2,'12:30:00','14:00:00','2025-11-19','COMPLETED', 2,'2025-11-18',NULL),
('0541112233','dana@mail.com',   3,'20:30:00','22:00:00','2025-11-21','ACCEPTED',  4,'2025-11-20','19:30:00'),    -- waitlist 60
('0500000000','guest10@mail.com',0,'21:00:00','22:30:00','2025-11-23','COMPLETED', 5,'2025-11-22','20:30:00'),    -- waitlist 30
('0532223344','amir@mail.com',   4,'18:00:00','19:30:00','2025-11-24','COMPLETED', 2,'2025-11-24',NULL),
('0500000000','guest11@mail.com',0,'13:10:00','15:00:00','2025-11-27','COMPLETED', 3,'2025-11-26',NULL),          -- late +2h
('0526948432','test@mail.com',   5,'19:30:00','21:30:00','2025-11-28','CANCELLED',  2,'2025-11-27',NULL),
('0500000000','guest21@mail.com',0,'12:30:00','14:00:00','2025-11-04','COMPLETED',2,'2025-11-03','12:00:00'), -- waitlist 30
('0500000000','guest22@mail.com',0,'18:00:00','19:45:00','2025-11-06','COMPLETED',6,'2025-11-05',NULL),
('0501234567','tal@mail.com',    1,'19:30:00','21:00:00','2025-11-08','ACCEPTED', 2,'2025-11-07','18:30:00'), -- waitlist 60
('0529876543','noam@mail.com',   2,'20:30:00','22:00:00','2025-11-13','COMPLETED',2,'2025-11-12','20:00:00'), -- waitlist 30
('0500000000','guest23@mail.com',0,'10:00:00','11:30:00','2025-11-14','COMPLETED',2,'2025-11-13',NULL),
('0541112233','dana@mail.com',   3,'21:10:00','22:10:00','2025-11-22','COMPLETED',3,'2025-11-21',NULL),        -- late +2h
('0500000000','guest24@mail.com',0,'11:30:00','13:00:00','2025-11-25','ACCEPTED', 2,'2025-11-25','10:30:00'),  -- waitlist 60
('0526948432','test@mail.com',   5,'18:30:00','20:30:00','2025-11-29','LATE_CANCEL',4,'2025-11-28','17:30:00'), -- waitlist 60
('0500000000','guest38@mail.com',0,'18:30:00','20:00:00','2025-11-02','ACCEPTED',2,'2025-11-01','17:30:00'),   -- day existed on-time => add waitlist
('0501234567','tal@mail.com',  1,'20:12:00','22:12:00','2025-11-03','COMPLETED',2,'2025-11-03',NULL),          -- day existed waitlist => add late(+2h)
('0500000000','guest39@mail.com',0,'10:30:00','12:00:00','2025-11-06','COMPLETED',4,'2025-11-05',NULL),        -- day existed cancelled => add completed
('0526948432','test@mail.com', 5,'12:00:00','13:30:00','2025-11-09','COMPLETED',2,'2025-11-08','11:30:00'),    -- day existed waitlist 90 => add waitlist 30 + on-time
('0500000000','guest40@mail.com',0,'19:30:00','21:10:00','2025-11-10','COMPLETED',5,'2025-11-09',NULL),        -- day existed on-time => add different duration
('0541112233','dana@mail.com', 3,'13:30:00','15:00:00','2025-11-11','ACCEPTED',2,'2025-11-10','12:30:00'),     -- day existed guest late => add waitlist/on-time
('0500000000','guest41@mail.com',0,'21:12:00','23:12:00','2025-11-15','LATE_CANCEL',2,'2025-11-15',NULL),      -- day existed waitlist => add late_cancel (+2h)
('0529876543','noam@mail.com', 2,'12:30:00','14:00:00','2025-11-18','COMPLETED',2,'2025-11-17',NULL),          -- day existed cancelled => add completed on-time
('0500000000','guest42@mail.com',0,'18:00:00','20:00:00','2025-11-20','ACCEPTED',4,'2025-11-19','17:00:00'),   -- day existed late => add waitlist/on-time
('0500000000','guest43@mail.com',0,'20:12:00','22:12:00','2025-11-23','COMPLETED',2,'2025-11-23',NULL),        -- day existed waitlist 30 => add late(+2h)
('0501234567','tal@mail.com',  1,'19:30:00','21:00:00','2025-11-28','ACCEPTED',2,'2025-11-27','18:30:00'),     -- day existed cancelled => add waitlist 60
('0500000000','guest44@mail.com',0,'11:00:00','13:00:00','2025-11-30','LATE_CANCEL',3,'2025-11-30','10:00:00'), -- missing day + late_cancel + waitlist 60
/* -------------------- DEC 2025 (6 orders) -------------------- */
('0501234567','tal@mail.com',        1,'12:00:00','14:00:00','2025-12-03','COMPLETED', 2,'2025-11-29',NULL),          -- on-time
('0500000000','guest@mail.com',      0,'19:30:00','21:30:00','2025-12-06','ACCEPTED',  3,'2025-12-05','18:30:00'),     -- guest + waitlist
('0529876543','noam@mail.com',       2,'20:14:00','22:00:00','2025-12-08','COMPLETED', 2,'2025-12-06',NULL),          -- late (<15)
('0541112233','dana@mail.com',       3,'13:30:00','15:30:00','2025-12-12','CANCELLED', 4,'2025-12-09',NULL),          -- cancelled
('0532223344','amir@mail.com',       4,'19:30:00','21:30:00','2025-12-18','COMPLETED', 6,'2025-12-14','19:00:00'),     -- waitlist
('0526948432','test@mail.com',       5,'11:10:00','13:00:00','2025-12-27','ACCEPTED',  2,'2025-12-26',NULL),
('0500000000','guest12@mail.com',0,'10:00:00','11:30:00','2025-12-02','COMPLETED', 4,'2025-12-01',NULL),
('0501234567','tal@mail.com',    1,'12:30:00','14:00:00','2025-12-04','ACCEPTED',  2,'2025-12-03','12:00:00'),    -- waitlist 30
('0529876543','noam@mail.com',   2,'18:30:00','20:00:00','2025-12-05','COMPLETED', 2,'2025-12-04','17:30:00'),    -- waitlist 60
('0500000000','guest13@mail.com',0,'20:10:00','22:00:00','2025-12-07','COMPLETED', 2,'2025-12-07',NULL),          -- late +2h
('0541112233','dana@mail.com',   3,'21:30:00','23:00:00','2025-12-09','LATE_CANCEL',2,'2025-12-08','20:30:00'),    -- waitlist 60 + late_cancel
('0532223344','amir@mail.com',   4,'13:30:00','15:00:00','2025-12-10','COMPLETED', 6,'2025-12-09',NULL),
('0500000000','guest14@mail.com',0,'19:30:00','21:00:00','2025-12-13','ACCEPTED',  2,'2025-12-13','18:00:00'),    -- waitlist 90
('0526948432','test@mail.com',   5,'11:14:00','13:00:00','2025-12-15','COMPLETED', 2,'2025-12-14',NULL),          -- late +2h
('0500000000','guest15@mail.com',0,'12:00:00','13:20:00','2025-12-16','CANCELLED',  2,'2025-12-15',NULL),
('0529876543','noam@mail.com',   2,'18:00:00','19:45:00','2025-12-20','COMPLETED', 4,'2025-12-18',NULL),
('0501234567','tal@mail.com',    1,'20:30:00','22:00:00','2025-12-22','ACCEPTED',  3,'2025-12-21','19:30:00'),    -- waitlist 60
('0500000000','guest16@mail.com',0,'21:30:00','23:30:00','2025-12-23','COMPLETED', 5,'2025-12-22','21:00:00'),
('0500000000','guest25@mail.com',0,'10:30:00','12:10:00','2025-12-01','COMPLETED',3,'2025-11-30',NULL),
('0501234567','tal@mail.com',    1,'18:00:00','20:00:00','2025-12-11','ACCEPTED', 2,'2025-12-10','16:00:00'),  -- waitlist 120
('0500000000','guest26@mail.com',0,'19:30:00','21:00:00','2025-12-14','COMPLETED',2,'2025-12-13','19:00:00'),  -- waitlist 30
('0529876543','noam@mail.com',   2,'20:30:00','22:30:00','2025-12-17','COMPLETED',4,'2025-12-16',NULL),
('0500000000','guest27@mail.com',0,'21:12:00','22:12:00','2025-12-19','COMPLETED',2,'2025-12-19',NULL),        -- late +2h
('0541112233','dana@mail.com',   3,'12:30:00','14:00:00','2025-12-21','CANCELLED',2,'2025-12-20',NULL),
('0500000000','guest28@mail.com',0,'13:30:00','15:30:00','2025-12-24','ACCEPTED', 4,'2025-12-23','12:30:00'),  -- waitlist 60
('0526948432','test@mail.com',   5,'18:30:00','20:10:00','2025-12-25','COMPLETED',2,'2025-12-24','18:00:00'),  -- waitlist 30
('0500000000','guest29@mail.com',0,'20:30:00','22:00:00','2025-12-26','LATE_CANCEL',3,'2025-12-26','19:00:00'), -- waitlist 90
('0501234567','tal@mail.com',    1,'11:30:00','13:20:00','2025-12-28','COMPLETED',2,'2025-12-27',NULL),
('0500000000','guest30@mail.com',0,'21:30:00','23:30:00','2025-12-29','ACCEPTED', 5,'2025-12-28','21:00:00') ,  -- waitlist 30
('0500000000','guest45@mail.com',0,'12:30:00','14:00:00','2025-12-04','COMPLETED',2,'2025-12-03',NULL),        -- day existed waitlist => add no-wait
('0529876543','noam@mail.com', 2,'18:12:00','20:12:00','2025-12-06','COMPLETED',2,'2025-12-06',NULL),          -- day existed on-time => add late(+2h)
('0500000000','guest46@mail.com',0,'10:30:00','12:00:00','2025-12-08','ACCEPTED',3,'2025-12-07','09:30:00'),   -- day existed late => add waitlist/on-time
('0501234567','tal@mail.com',  1,'20:30:00','22:10:00','2025-12-09','COMPLETED',2,'2025-12-08',NULL),          -- day existed late_cancel => add completed on-time
('0500000000','guest47@mail.com',0,'21:12:00','23:12:00','2025-12-12','LATE_CANCEL',2,'2025-12-12','20:00:00'),-- day existed cancelled => add late_cancel + waitlist 72? (waitlist enter ok; actual wait you compute)
('0526948432','test@mail.com', 5,'12:00:00','13:30:00','2025-12-14','COMPLETED',2,'2025-12-13',NULL),          -- day existed waitlist => add no-wait
('0500000000','guest48@mail.com',0,'18:30:00','20:00:00','2025-12-15','ACCEPTED',4,'2025-12-14','17:30:00'),   -- day existed late => add waitlist/on-time
('0541112233','dana@mail.com', 3,'20:12:00','22:12:00','2025-12-16','COMPLETED',2,'2025-12-16',NULL),          -- day existed cancelled => add late(+2h)
('0500000000','guest49@mail.com',0,'12:30:00','14:30:00','2025-12-18','ACCEPTED',2,'2025-12-17','11:30:00'),   -- day existed completed+waitlist => add accepted+waitlist
('0529876543','noam@mail.com', 2,'21:12:00','23:12:00','2025-12-20','LATE_CANCEL',2,'2025-12-20',NULL),         -- day existed completed => add late_cancel (+2h)
('0500000000','guest50@mail.com',0,'10:30:00','12:00:00','2025-12-22','COMPLETED',3,'2025-12-21',NULL),        -- day existed waitlist => add no-wait
('0500000000','guest51@mail.com',0,'19:30:00','21:00:00','2025-12-27','COMPLETED',2,'2025-12-26','18:30:00') ,  -- same day existed => add waitlist 60
('0500000000','guest51@mail.com',0,'19:30:00','21:00:00','2025-12-27','COMPLETED',2,'2025-12-26','18:30:00'),  -- same day existed => add waitlist 60

('0501234567','tal@mail.com',0,'08:30:00','10:30:00','2026-01-18','CONFIRMED',1,'2026-01-17',NULL),
('0501234567','tal@mail.com',0,'09:00:00','11:00:00','2026-01-18','CONFIRMED',4,'2026-01-17',NULL),
('0501234567','tal@mail.com',0,'08:00:00','10:00:00','2026-01-18','CONFIRMED',6,'2026-01-17',NULL)
;
        -- late (<15)
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriber`
--

DROP TABLE IF EXISTS `subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriber` (
  `sub_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(10) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  PRIMARY KEY (`sub_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber`
--

LOCK TABLES `subscriber` WRITE;
/*!40000 ALTER TABLE `subscriber` DISABLE KEYS */;
INSERT INTO `subscriber` VALUES 
(0,'guest','guest','guest','guest@mail.com','0500000000','lol'),
(1,'tal123','Tal','Levi','tal@mail.com','0501234567','$2a$10$talhash'),
(2,'noam77','Noam','Cohen','noam@mail.com','0529876543','$2a$10$noamhash'),
(3,'dana_k','Dana','Katz','dana@mail.com','0541112233','$2a$10$danahash'),
(4,'amir90','Amir','Ben-David','amir@mail.com','0532223344','$2a$10$amirhash'),
('5', 'test', 'test', 'test', 'test@mail.com', '0526948432', '$2a$10$zGVDSeVGYOhcShbBB9ZwF.73fM6B3yIR6xkm3atXzy69X0RHYDWMy');
/*!40000 ALTER TABLE `subscriber` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tablestable`
--

DROP TABLE IF EXISTS `tablestable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tablestable` (
  `table_number` int NOT NULL AUTO_INCREMENT,
  `size` enum('2','4','6','8','10') NOT NULL,
  `res_id` int DEFAULT NULL,
  PRIMARY KEY (`table_number`),
  KEY `res_id` (`res_id`),
  CONSTRAINT `tablestable_ibfk_1` FOREIGN KEY (`res_id`) REFERENCES `reservations` (`res_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tablestable`
--

LOCK TABLES `tablestable` WRITE;
/*!40000 ALTER TABLE `tablestable` DISABLE KEYS */;
INSERT INTO `tablestable` VALUES 
(1,'2',NULL),
(2,'4',NULL),
(3,'6',NULL),
(4,'8',NULL),
(5,'10',NULL),
(6,'6',NULL);
/*!40000 ALTER TABLE `tablestable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workers`
--

DROP TABLE IF EXISTS `workers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workers` (
  `worker_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `worker_type` enum('employee','manager') NOT NULL,

  `manager_guard` tinyint
    GENERATED ALWAYS AS (
      CASE WHEN `worker_type` = 'manager' THEN 1 ELSE NULL END
    ) STORED,

  PRIMARY KEY (`worker_id`),
  UNIQUE KEY `uq_single_manager` (`manager_guard`),
  UNIQUE KEY `uq_workers_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=90866 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workers`
--

LOCK TABLES `workers` WRITE;
/*!40000 ALTER TABLE `workers` DISABLE KEYS */;

INSERT INTO `workers` 
(`worker_id`, `username`, `password_hash`, `worker_type`)
VALUES 
(12345, 'test', '$2a$10$zGVDSeVGYOhcShbBB9ZwF.73fM6B3yIR6xkm3atXzy69X0RHYDWMy', 'manager'),
 (15357,'lior22','$2a$10$liorhash','employee'),
 (74839,'shira7','$2a$10$shirahash','employee'),
 (86096,'yael99','$2a$10$yaelhash','employee'),
 (90865,'omer_x','$2a$10$omerhash','employee'),
 (90866, 'talmetz100', '$2a$10$AkIaIxSA67DBz3KWKzFBWONifu9eYOXsXLpaeGSx7gUilCX3CE7jC', 'employee'),
 (54321, 'worker', '12345', 'employee');
/*!40000 ALTER TABLE `workers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `regulartimes`
--

DROP TABLE IF EXISTS `regulartimes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `regulartimes` (
  `day` ENUM ('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday') NOT NULL,
  `opening_time` TIME DEFAULT NULL,
  `closing_time` TIME DEFAULT NULL,
  PRIMARY KEY (`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `regulartimes`
--

LOCK TABLES `regulartimes` WRITE;
/*!40000 ALTER TABLE `regulartimes` DISABLE KEYS */;
INSERT INTO `regulartimes` 
(`day`, `opening_time`, `closing_time`)
VALUES ('Sunday', '10:00:00', '22:00:00'), 
('Monday', '10:00:00', '22:00:00'), 
('Tuesday', '10:00:00', '22:00:00'), 
('Wednesday', '10:00:00', '22:00:00'), 
('Thursday', '10:00:00', '22:00:00'), 
('Friday', '10:00:00', '15:00:00'),
('Saturday', '20:00:00', '23:00:00');
/*!40000 ALTER TABLE `regulartimes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `specialdates`
--

DROP TABLE IF EXISTS `specialdates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `specialdates` (
  `date` DATE NOT NULL,
  `opening_time` TIME DEFAULT NULL,
  `closing_time` TIME DEFAULT NULL,
  PRIMARY KEY (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `specialdates`
--

LOCK TABLES `specialdates` WRITE;
/*!40000 ALTER TABLE `specialdates` DISABLE KEYS */;
INSERT INTO `specialdates` 
(`date`, `opening_time`, `closing_time`)
VALUES 
('2026-04-01', '09:00:00', '13:00:00'), 
('2026-01-18','08:00:00', '23:00:00'),
('2026-04-02', '00:00:00', '00:00:00'),             
('2026-04-08', '00:00:00', '00:00:00'),
('2026-07-22', '10:00:00', '19:00:00'), 
('2026-07-23','00:00:00', '00:00:00');
/*!40000 ALTER TABLE `specialdates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'bistro'
--

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-28 22:04:33

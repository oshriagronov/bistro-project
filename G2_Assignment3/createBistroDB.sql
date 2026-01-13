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
  `order_status` enum('PENDING', 'REMINDED', 'CONFIRMED','CANCELLED','COMPLETED', 'ACCEPTED') DEFAULT NULL,
  `num_diners` int NOT NULL,
  `date_of_placing_order` date NOT NULL,

  -- ✅ NEW: waitlist enter timestamp (NULL for regular reservations)
  `waitlist_enter_time` DATETIME NULL,

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
('0526943286', 'test@mail.com', 5, '18:00:00', '20:00:00', '2025-01-06', 'CONFIRMED', 2, '2024-12-31', NULL);
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriber`
--
/*
  subscriber for test is: sub_id = 5, hash_password = test
*/
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
/*
  subscriber for test is: sub_id = 5, password = test
*/
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
/*
  employee for test is: sub_id = 5, hash_password = test
*/
DROP TABLE IF EXISTS `workers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workers` (
  `worker_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `worker_type` enum('employee','manager') NOT NULL,
  PRIMARY KEY (`worker_id`)
) ENGINE=InnoDB AUTO_INCREMENT=90866 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workers`
--

LOCK TABLES `workers` WRITE;
/*!40000 ALTER TABLE `workers` DISABLE KEYS */;
INSERT INTO `workers` VALUES 
('12345', 'test', '$2a$10$zGVDSeVGYOhcShbBB9ZwF.73fM6B3yIR6xkm3atXzy69X0RHYDWMy', 'employee'),
 (15357,'lior22','$2a$10$liorhash','manager'),
 (74839,'shira7','$2a$10$shirahash','employee'),
 (86096,'yael99','$2a$10$yaelhash','employee'),
 (90865,'omer_x','$2a$10$omerhash','employee'),
 (90866, 'talmetz100', '$2a$10$AkIaIxSA67DBz3KWKzFBWONifu9eYOXsXLpaeGSx7gUilCX3CE7jC', 'manager'),
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
('2026-04-02', NULL, NULL),             
('2026-04-08', NULL, NULL),
('2026-07-22', '10:00:00', '19:00:00'), 
('2026-07-23', NULL, NULL);
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

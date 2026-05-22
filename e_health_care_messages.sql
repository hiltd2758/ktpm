-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: e_health_care
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `room` varchar(255) NOT NULL,
  `sender` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `room_idx` (`room`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,'chat_3_nguyenledaiphi0252005@gmail.com','nguyenledaiphi0252005@gmail.com','Hey','2025-11-24 13:43:30'),(2,'chat_3_nguyenledaiphi0252005@gmail.com','nguyenledaiphi0252005@gmail.com','bageaga','2025-11-24 13:48:24'),(3,'chat_3_nguyenledaiphi0252005@gmail.com','nguyenledaiphi0252005@gmail.com','Con gái mẹ mày ','2025-11-24 13:48:33'),(4,'chat_4_nguyenledaiphi0252005@gmail.com','nguyenledaiphi0252005@gmail.com','Hey','2025-11-24 13:49:46'),(5,'chat_1_thanh@gmail.com','thanh@gmail.com','Hey','2025-11-24 13:50:03'),(6,'chat_4_nguyenledaiphi0252005@gmail.com','nguyenledaiphi0252005@gmail.com','Uây.','2025-11-25 14:33:30'),(7,'chat_nguyenledaiphi0252005@gmail.com_phiproga@gmail.com','phiproga@gmail.com','Hey','2025-11-25 14:47:22'),(8,'chat_nguyenledaiphi0252005@gmail.com_phiproga@gmail.com','nguyenledaiphi0252005@gmail.com','Chào anh.','2025-11-25 14:48:21'),(9,'chat_nguyenledaiphi0252005@gmail.com_phiproga@gmail.com','nguyenledaiphi0252005@gmail.com','Em yêu anh.','2025-11-25 14:49:39'),(10,'chat_nguyenledaiphi0252005@gmail.com_phiproga@gmail.com','phiproga@gmail.com','Anh yêu em','2025-11-25 14:50:09'),(11,'chat_nguyenledaiphi0252005@gmail.com_thanh@gmail.com','nguyenledaiphi0252005@gmail.com','Chào anh, tôi là bác sĩ John Doe.','2025-11-25 14:50:39'),(12,'chat_nguyenledaiphi0252005@gmail.com_thanh@gmail.com','thanh@gmail.com','Chào anh.','2025-11-25 14:51:02');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 22:08:20

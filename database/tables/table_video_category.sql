DROP PROCEDURE IF EXISTS `p_manage_video_category`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_category`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_category';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_category` (
          `CategoryCod` varchar(16) NOT NULL,
          `Name` varchar(128) NOT NULL,
          `Description` varchar(512) DEFAULT NULL,
          `ImageUrl` varchar(512) DEFAULT NULL,
          `DisplayOrder` int NOT NULL DEFAULT 0,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`CategoryCod`),
          UNIQUE KEY `uk_video_category_name` (`Name`),
          KEY `idx_video_category_status_order` (`Status`, `DisplayOrder`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_category creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla video_category ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_category`();
DROP PROCEDURE `p_manage_video_category`;

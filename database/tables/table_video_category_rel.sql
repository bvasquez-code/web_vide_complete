DROP PROCEDURE IF EXISTS `p_manage_video_category_rel`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_category_rel`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_category_rel';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_category_rel` (
          `VideoCod` varchar(16) NOT NULL,
          `CategoryCod` varchar(16) NOT NULL,
          `IsPrimary` char(1) NOT NULL DEFAULT 'N',
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`VideoCod`, `CategoryCod`),
          KEY `fk_video_category_rel_category` (`CategoryCod`),
          KEY `idx_video_category_rel_status` (`Status`),
          CONSTRAINT `fk_video_category_rel_video` FOREIGN KEY (`VideoCod`) REFERENCES `video` (`VideoCod`),
          CONSTRAINT `fk_video_category_rel_category` FOREIGN KEY (`CategoryCod`) REFERENCES `video_category` (`CategoryCod`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_category_rel creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla video_category_rel ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_category_rel`();
DROP PROCEDURE `p_manage_video_category_rel`;

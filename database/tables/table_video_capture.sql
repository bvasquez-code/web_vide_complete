DROP PROCEDURE IF EXISTS `p_manage_video_capture`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_capture`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_capture';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_capture` (
          `CaptureId` bigint NOT NULL AUTO_INCREMENT,
          `VideoCod` varchar(16) NOT NULL,
          `ImageUrl` varchar(512) NOT NULL,
          `CaptureSecond` decimal(12,3) NOT NULL,
          `DisplayOrder` int NOT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`CaptureId`),
          KEY `fk_video_capture_video` (`VideoCod`),
          KEY `idx_video_capture_order` (`VideoCod`, `Status`, `CaptureSecond`, `DisplayOrder`),
          CONSTRAINT `fk_video_capture_video` FOREIGN KEY (`VideoCod`) REFERENCES `video` (`VideoCod`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_capture creada desde cero.' AS Mensaje;
    ELSE
        IF EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'video_capture'
              AND index_name = 'idx_video_capture_order'
              AND column_name = 'DisplayOrder'
              AND seq_in_index = 3
        ) THEN
            DROP INDEX `idx_video_capture_order` ON `video_capture`;
            CREATE INDEX `idx_video_capture_order` ON `video_capture` (`VideoCod`, `Status`, `CaptureSecond`, `DisplayOrder`);
            SELECT 'Indice idx_video_capture_order actualizado para ordenar por segundo de captura.' AS Mensaje;
        ELSE
            SELECT 'Tabla video_capture ya existe. No se realizaron cambios estructurales.' AS Mensaje;
        END IF;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_capture`();
DROP PROCEDURE `p_manage_video_capture`;

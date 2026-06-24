DROP PROCEDURE IF EXISTS `p_manage_video_view_log`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_view_log`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_view_log';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_view_log` (
          `ViewLogId` bigint NOT NULL AUTO_INCREMENT,
          `VideoCod` varchar(16) NOT NULL,
          `ViewerUserCod` varchar(16) DEFAULT NULL,
          `ViewerIp` varchar(64) DEFAULT NULL,
          `UserAgent` varchar(512) DEFAULT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`ViewLogId`),
          KEY `fk_video_view_log_video` (`VideoCod`),
          KEY `idx_video_view_log_viewer` (`ViewerUserCod`, `CreationDate`),
          KEY `idx_video_view_log_creation` (`CreationDate`),
          CONSTRAINT `fk_video_view_log_video` FOREIGN KEY (`VideoCod`) REFERENCES `video` (`VideoCod`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_view_log creada desde cero.' AS Mensaje;
    ELSE
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'video_view_log'
              AND column_name = 'ViewerUserCod'
        ) THEN
            ALTER TABLE `video_view_log`
              ADD COLUMN `ViewerUserCod` varchar(16) DEFAULT NULL AFTER `VideoCod`;

            SELECT 'Columna ViewerUserCod agregada a video_view_log.' AS Mensaje;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'video_view_log'
              AND index_name = 'idx_video_view_log_viewer'
        ) THEN
            CREATE INDEX `idx_video_view_log_viewer` ON `video_view_log` (`ViewerUserCod`, `CreationDate`);
            SELECT 'Indice idx_video_view_log_viewer creado.' AS Mensaje;
        ELSE
            SELECT 'Tabla video_view_log ya existe. No se realizaron cambios estructurales.' AS Mensaje;
        END IF;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_view_log`();
DROP PROCEDURE `p_manage_video_view_log`;

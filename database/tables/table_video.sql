DROP PROCEDURE IF EXISTS `p_manage_video`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video` (
          `VideoCod` varchar(16) NOT NULL,
          `Title` varchar(180) NOT NULL,
          `ShortDescription` varchar(512) DEFAULT NULL,
          `LongDescription` text,
          `ThumbnailUrl` varchar(512) DEFAULT NULL,
          `SourceType` varchar(16) NOT NULL,
          `SourceValue` text NOT NULL,
          `Duration` varchar(32) DEFAULT NULL,
          `ViewCount` bigint NOT NULL DEFAULT 0,
          `PublishDate` datetime DEFAULT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`VideoCod`),
          KEY `idx_video_status_creation` (`Status`, `CreationDate`),
          KEY `idx_video_view_count` (`Status`, `ViewCount`),
          KEY `idx_video_source_type` (`SourceType`),
          FULLTEXT KEY `idx_video_title_description` (`Title`, `ShortDescription`, `LongDescription`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla video ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video`();
DROP PROCEDURE `p_manage_video`;

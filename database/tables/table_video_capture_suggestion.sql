DROP PROCEDURE IF EXISTS `p_manage_video_capture_suggestion`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_capture_suggestion`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_capture_suggestion';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_capture_suggestion` (
          `SuggestionId` bigint NOT NULL AUTO_INCREMENT,
          `VideoCod` varchar(16) NOT NULL,
          `SubscriberUserCod` varchar(16) NOT NULL,
          `ImageUrl` varchar(512) NOT NULL,
          `CaptureSecond` decimal(12,3) NOT NULL,
          `Comment` varchar(512) DEFAULT NULL,
          `ReviewComment` varchar(512) DEFAULT NULL,
          `ApprovedCaptureId` bigint DEFAULT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'P',
          PRIMARY KEY (`SuggestionId`),
          KEY `idx_video_capture_suggestion_pending` (`Status`, `CreationDate`),
          KEY `fk_video_capture_suggestion_video` (`VideoCod`),
          KEY `fk_video_capture_suggestion_subscriber` (`SubscriberUserCod`),
          CONSTRAINT `fk_video_capture_suggestion_video` FOREIGN KEY (`VideoCod`) REFERENCES `video` (`VideoCod`),
          CONSTRAINT `fk_video_capture_suggestion_subscriber` FOREIGN KEY (`SubscriberUserCod`) REFERENCES `subscriber_user` (`SubscriberUserCod`),
          CONSTRAINT `fk_video_capture_suggestion_capture` FOREIGN KEY (`ApprovedCaptureId`) REFERENCES `video_capture` (`CaptureId`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_capture_suggestion creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla video_capture_suggestion ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_capture_suggestion`();
DROP PROCEDURE `p_manage_video_capture_suggestion`;

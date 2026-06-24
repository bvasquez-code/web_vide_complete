DROP PROCEDURE IF EXISTS `p_manage_video_reaction`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_reaction`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_reaction';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_reaction` (
          `ReactionId` bigint NOT NULL AUTO_INCREMENT,
          `VideoCod` varchar(16) NOT NULL,
          `SubscriberUserCod` varchar(16) NOT NULL,
          `ReactionType` varchar(16) NOT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`ReactionId`),
          UNIQUE KEY `uk_video_reaction_user` (`VideoCod`, `SubscriberUserCod`),
          KEY `idx_video_reaction_type` (`VideoCod`, `ReactionType`, `Status`),
          KEY `fk_video_reaction_subscriber` (`SubscriberUserCod`),
          CONSTRAINT `fk_video_reaction_video` FOREIGN KEY (`VideoCod`) REFERENCES `video` (`VideoCod`),
          CONSTRAINT `fk_video_reaction_subscriber` FOREIGN KEY (`SubscriberUserCod`) REFERENCES `subscriber_user` (`SubscriberUserCod`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_reaction creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla video_reaction ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_reaction`();
DROP PROCEDURE `p_manage_video_reaction`;

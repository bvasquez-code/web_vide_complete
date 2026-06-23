DROP PROCEDURE IF EXISTS `p_manage_video_actor_rel`;

DELIMITER $$

CREATE PROCEDURE `p_manage_video_actor_rel`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'video_actor_rel';

    IF v_table_exists = 0 THEN
        CREATE TABLE `video_actor_rel` (
          `VideoCod` varchar(16) NOT NULL,
          `ActorCod` varchar(16) NOT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`VideoCod`, `ActorCod`),
          KEY `fk_video_actor_rel_actor` (`ActorCod`),
          KEY `idx_video_actor_rel_status` (`Status`),
          CONSTRAINT `fk_video_actor_rel_video` FOREIGN KEY (`VideoCod`) REFERENCES `video` (`VideoCod`),
          CONSTRAINT `fk_video_actor_rel_actor` FOREIGN KEY (`ActorCod`) REFERENCES `actor` (`ActorCod`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla video_actor_rel creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla video_actor_rel ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_video_actor_rel`();
DROP PROCEDURE `p_manage_video_actor_rel`;

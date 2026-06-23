DROP PROCEDURE IF EXISTS `p_manage_actor`;

DELIMITER $$

CREATE PROCEDURE `p_manage_actor`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'actor';

    IF v_table_exists = 0 THEN
        CREATE TABLE `actor` (
          `ActorCod` varchar(16) NOT NULL,
          `Name` varchar(128) NOT NULL,
          `Description` varchar(512) DEFAULT NULL,
          `ImageUrl` varchar(512) DEFAULT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`ActorCod`),
          UNIQUE KEY `uk_actor_name` (`Name`),
          KEY `idx_actor_status` (`Status`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla actor creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla actor ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_actor`();
DROP PROCEDURE `p_manage_actor`;

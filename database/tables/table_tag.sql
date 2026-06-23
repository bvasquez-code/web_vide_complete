DROP PROCEDURE IF EXISTS `p_manage_tag`;

DELIMITER $$

CREATE PROCEDURE `p_manage_tag`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'tag';

    IF v_table_exists = 0 THEN
        CREATE TABLE `tag` (
          `TagCod` varchar(16) NOT NULL,
          `Name` varchar(128) NOT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`TagCod`),
          UNIQUE KEY `uk_tag_name` (`Name`),
          KEY `idx_tag_status` (`Status`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla tag creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla tag ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_tag`();
DROP PROCEDURE `p_manage_tag`;

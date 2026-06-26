DROP PROCEDURE IF EXISTS `p_manage_desktop_app_config`;

DELIMITER $$

CREATE PROCEDURE `p_manage_desktop_app_config`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'desktop_app_config';

    IF v_table_exists = 0 THEN
        CREATE TABLE `desktop_app_config` (
          `ConfigKey` varchar(64) NOT NULL,
          `ConfigValue` varchar(1024) NOT NULL,
          `Description` varchar(256) DEFAULT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`ConfigKey`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        INSERT INTO `desktop_app_config` (`ConfigKey`, `ConfigValue`, `Description`, `CreationUser`, `Status`)
        VALUES ('UPLOAD_ROOT', 'uploads', 'Ruta base local de uploads para miniaturas y capturas en escritorio.', 'SISTEMA', 'A');

        SELECT 'Tabla desktop_app_config creada desde cero.' AS Mensaje;
    ELSE
        INSERT INTO `desktop_app_config` (`ConfigKey`, `ConfigValue`, `Description`, `CreationUser`, `Status`)
        SELECT 'UPLOAD_ROOT', 'uploads', 'Ruta base local de uploads para miniaturas y capturas en escritorio.', 'SISTEMA', 'A'
        WHERE NOT EXISTS (
            SELECT 1 FROM `desktop_app_config` WHERE `ConfigKey` = 'UPLOAD_ROOT'
        );

        SELECT 'Tabla desktop_app_config ya existe. Configuracion base validada.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_desktop_app_config`();
DROP PROCEDURE `p_manage_desktop_app_config`;

DROP PROCEDURE IF EXISTS `p_manage_admin_user`;

DELIMITER $$

CREATE PROCEDURE `p_manage_admin_user`()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'admin_user';

    IF v_table_exists = 0 THEN
        CREATE TABLE `admin_user` (
          `AdminUserCod` varchar(16) NOT NULL,
          `UserName` varchar(64) NOT NULL,
          `PasswordHash` varchar(128) NOT NULL,
          `Names` varchar(128) DEFAULT NULL,
          `LastLoginDate` datetime DEFAULT NULL,
          `CreationUser` varchar(16) NOT NULL,
          `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `ModifyUser` varchar(16) DEFAULT NULL,
          `ModifyDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          `Status` char(1) NOT NULL DEFAULT 'A',
          PRIMARY KEY (`AdminUserCod`),
          UNIQUE KEY `uk_admin_user_username` (`UserName`),
          KEY `idx_admin_user_status` (`Status`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

        SELECT 'Tabla admin_user creada desde cero.' AS Mensaje;
    ELSE
        SELECT 'Tabla admin_user ya existe. No se realizaron cambios estructurales.' AS Mensaje;
    END IF;
END $$

DELIMITER ;

CALL `p_manage_admin_user`();
DROP PROCEDURE `p_manage_admin_user`;

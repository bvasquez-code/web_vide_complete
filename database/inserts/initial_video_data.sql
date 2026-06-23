INSERT INTO `admin_user` (`AdminUserCod`, `UserName`, `PasswordHash`, `Names`, `CreationUser`, `Status`)
SELECT 'AD000001', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador', 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `admin_user` WHERE `AdminUserCod` = 'AD000001');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00001', 'Terror', 'Videos de terror y suspenso.', 1, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00001');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00002', 'Videos graciosos', 'Contenido de humor y clips divertidos.', 2, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00002');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00003', 'Videos de gatos', 'Videos de gatos y mascotas.', 3, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00003');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00004', 'Accion', 'Videos de accion.', 4, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00004');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00005', 'Musica', 'Videos musicales.', 5, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00005');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00006', 'Documentales', 'Documentales y contenido educativo.', 6, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00006');

INSERT INTO `video_category` (`CategoryCod`, `Name`, `Description`, `DisplayOrder`, `CreationUser`, `Status`)
SELECT 'CAT00007', 'Animacion', 'Videos animados.', 7, 'SISTEMA', 'A'
WHERE NOT EXISTS (SELECT 1 FROM `video_category` WHERE `CategoryCod` = 'CAT00007');

UPDATE `video`
SET `ThumbnailUrl` = SUBSTRING(`ThumbnailUrl`, LOCATE('/api/v1/public/thumbnails/', `ThumbnailUrl`))
WHERE `ThumbnailUrl` IS NOT NULL
  AND `ThumbnailUrl` <> ''
  AND LOCATE('/api/v1/public/thumbnails/', `ThumbnailUrl`) > 0
  AND `ThumbnailUrl` NOT LIKE '/api/v1/public/thumbnails/%';

UPDATE `video_capture`
SET `ImageUrl` = SUBSTRING(`ImageUrl`, LOCATE('/api/v1/public/captures/', `ImageUrl`))
WHERE `ImageUrl` IS NOT NULL
  AND `ImageUrl` <> ''
  AND LOCATE('/api/v1/public/captures/', `ImageUrl`) > 0
  AND `ImageUrl` NOT LIKE '/api/v1/public/captures/%';

UPDATE `video_capture_suggestion`
SET `ImageUrl` = SUBSTRING(`ImageUrl`, LOCATE('/api/v1/public/captures/', `ImageUrl`))
WHERE `ImageUrl` IS NOT NULL
  AND `ImageUrl` <> ''
  AND LOCATE('/api/v1/public/captures/', `ImageUrl`) > 0
  AND `ImageUrl` NOT LIKE '/api/v1/public/captures/%';

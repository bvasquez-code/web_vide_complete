package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.entity.VideoEntity;
import com.ccadmin.app.video.repository.VideoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ThumbnailStorageService extends SessionService {
    private final VideoRepository videoRepository;
    private final Path thumbnailPath = Path.of("uploads", "thumbnails");

    public ThumbnailStorageService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Transactional
    public String saveThumbnail(String videoCod, MultipartFile file, HttpServletRequest request) throws Exception {
        if (videoCod == null || videoCod.isBlank()) {
            throw new IllegalArgumentException("Codigo de video obligatorio.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Imagen obligatoria.");
        }
        VideoEntity video = videoRepository.findById(videoCod)
                .orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));

        Files.createDirectories(thumbnailPath);
        String fileName = videoCod + "-" + System.currentTimeMillis() + ".jpg";
        Path target = thumbnailPath.resolve(fileName).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + "/api/v1/public/thumbnails/" + fileName;
        video.ThumbnailUrl = url;
        video.addSessionModify(getUserCod());
        videoRepository.save(video);
        return url;
    }

    public Path findThumbnail(String fileName) {
        Path target = thumbnailPath.resolve(fileName).normalize();
        if (!target.startsWith(thumbnailPath.normalize())) {
            throw new IllegalArgumentException("Nombre de miniatura invalido.");
        }
        return target;
    }
}

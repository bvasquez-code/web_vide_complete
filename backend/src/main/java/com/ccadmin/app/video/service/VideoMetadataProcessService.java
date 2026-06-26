package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.dto.VideoMetadataProcessItemDto;
import com.ccadmin.app.video.model.dto.VideoMetadataProcessResultDto;
import com.ccadmin.app.video.model.entity.VideoEntity;
import com.ccadmin.app.video.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class VideoMetadataProcessService extends SessionService {
    private static final String PUBLIC_THUMBNAIL_PATH = "/api/v1/public/thumbnails/";
    private final VideoRepository videoRepository;
    private final VideoPathService videoPathService;
    private final TransactionTemplate transactionTemplate;
    private final Path thumbnailPath = Path.of("uploads", "thumbnails");
    private final Integer commitBatchSize = 20;
    private final Integer fileMetadataCommitBatchSize = 50;

    public VideoMetadataProcessService(VideoRepository videoRepository, VideoPathService videoPathService, PlatformTransactionManager transactionManager) {
        this.videoRepository = videoRepository;
        this.videoPathService = videoPathService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public VideoMetadataProcessResultDto process(Double percentage, String videoCod, String mode, Boolean overwrite, String baseUrl) {
        double safePercentage = validatePercentage(percentage);
        String processMode = resolveProcessMode(mode, overwrite);
        List<VideoEntity> videos = resolveVideos(videoCod);

        VideoMetadataProcessResultDto result = new VideoMetadataProcessResultDto();
        result.TotalVideos = videos.size();
        result.Percentage = safePercentage;

        for (int index = 0; index < videos.size(); index += commitBatchSize) {
            final int startIndex = index;
            final int endIndex = Math.min(index + commitBatchSize, videos.size());
            List<VideoMetadataProcessItemDto> batchItems = transactionTemplate.execute(status -> {
                List<VideoMetadataProcessItemDto> items = new ArrayList<>();
                for (VideoEntity video : videos.subList(startIndex, endIndex)) {
                    items.add(processVideo(video, safePercentage, processMode, baseUrl));
                }
                return items;
            });
            if (batchItems == null) {
                continue;
            }
            for (VideoMetadataProcessItemDto item : batchItems) {
                addResultItem(result, item);
            }
        }

        return result;
    }

    public VideoMetadataProcessResultDto processFileMetadata(String videoCod, Boolean overwrite) {
        boolean overwriteExisting = Boolean.TRUE.equals(overwrite);
        List<VideoEntity> videos = resolveVideos(videoCod).stream()
                .filter(video -> "PATH".equals(video.SourceType))
                .filter(video -> overwriteExisting || missingFileMetadata(video))
                .toList();

        VideoMetadataProcessResultDto result = new VideoMetadataProcessResultDto();
        result.TotalVideos = videos.size();

        for (int index = 0; index < videos.size(); index += fileMetadataCommitBatchSize) {
            final int startIndex = index;
            final int endIndex = Math.min(index + fileMetadataCommitBatchSize, videos.size());
            List<VideoMetadataProcessItemDto> batchItems = transactionTemplate.execute(status -> {
                List<VideoMetadataProcessItemDto> items = new ArrayList<>();
                for (VideoEntity video : videos.subList(startIndex, endIndex)) {
                    items.add(processFileMetadataVideo(video));
                }
                return items;
            });
            if (batchItems == null) {
                continue;
            }
            for (VideoMetadataProcessItemDto item : batchItems) {
                addResultItem(result, item);
            }
        }
        return result;
    }

    public void applyFileMetadataIfAvailable(VideoEntity video) {
        try {
            if (!"PATH".equals(video.SourceType) || !hasText(video.SourceValue)) {
                return;
            }
            Path source = videoPathService.resolveSourcePath(video.SourceValue);
            if (!Files.exists(source) || !Files.isRegularFile(source) || !Files.isReadable(source)) {
                return;
            }
            applyFileMetadata(video, source);
        } catch (Exception ignored) {
        }
    }

    private VideoMetadataProcessItemDto processVideo(VideoEntity video, double percentage, String processMode, String baseUrl) {
        VideoMetadataProcessItemDto item = new VideoMetadataProcessItemDto();
        item.VideoCod = video.VideoCod;
        item.Title = video.Title;

        try {
            if (!"PATH".equals(video.SourceType)) {
                return skipped(item, "Solo se procesan videos con SourceType PATH.");
            }
            if ("MISSING_THUMBNAIL".equals(processMode) && hasText(video.ThumbnailUrl)) {
                item.Duration = video.Duration;
                item.ThumbnailUrl = video.ThumbnailUrl;
                return skipped(item, "El video ya tiene miniatura.");
            }

            Path source = videoPathService.resolveSourcePath(video.SourceValue);
            if (!Files.exists(source) || !Files.isRegularFile(source) || !Files.isReadable(source)) {
                return error(item, "Archivo de video no encontrado o sin permiso de lectura.");
            }

            double totalSeconds = readDurationSeconds(source);
            if (!Double.isFinite(totalSeconds) || totalSeconds <= 0) {
                return error(item, "No se pudo obtener una duracion valida.");
            }

            Files.createDirectories(thumbnailPath);
            String fileName = video.VideoCod + "-auto-" + System.currentTimeMillis() + ".jpg";
            Path target = thumbnailPath.resolve(fileName).normalize();
            double captureSecond = Math.max(0.1, totalSeconds * (percentage / 100.0));
            captureSecond = Math.min(captureSecond, Math.max(0.1, totalSeconds - 0.1));
            captureThumbnail(source, target, captureSecond);

            video.Duration = formatDuration(totalSeconds);
            video.ThumbnailUrl = PUBLIC_THUMBNAIL_PATH + fileName;
            applyFileMetadata(video, source);
            video.addSessionModify(getUserCod());
            videoRepository.save(video);

            item.Status = "OK";
            item.Message = "Metadata actualizada.";
            item.Duration = video.Duration;
            item.ThumbnailUrl = video.ThumbnailUrl;
            fillFileMetadataItem(item, video);
            return item;
        } catch (Exception ex) {
            return error(item, ex.getMessage());
        }
    }

    private VideoMetadataProcessItemDto processFileMetadataVideo(VideoEntity video) {
        VideoMetadataProcessItemDto item = new VideoMetadataProcessItemDto();
        item.VideoCod = video.VideoCod;
        item.Title = video.Title;
        try {
            if (!"PATH".equals(video.SourceType)) {
                return skipped(item, "Solo se procesan videos con SourceType PATH.");
            }
            Path source = videoPathService.resolveSourcePath(video.SourceValue);
            if (!Files.exists(source) || !Files.isRegularFile(source) || !Files.isReadable(source)) {
                return error(item, "Archivo de video no encontrado o sin permiso de lectura.");
            }
            applyFileMetadata(video, source);
            video.addSessionModify(getUserCod());
            videoRepository.save(video);
            item.Status = "OK";
            item.Message = "Peso y resolucion actualizados.";
            item.Duration = video.Duration;
            item.ThumbnailUrl = video.ThumbnailUrl;
            fillFileMetadataItem(item, video);
            return item;
        } catch (Exception ex) {
            return error(item, ex.getMessage());
        }
    }

    private void applyFileMetadata(VideoEntity video, Path source) throws Exception {
        video.FileSizeBytes = Files.size(source);
        ResolutionValue resolution = readResolution(source);
        video.ResolutionWidth = resolution.Width();
        video.ResolutionHeight = resolution.Height();
    }

    private void addResultItem(VideoMetadataProcessResultDto result, VideoMetadataProcessItemDto item) {
        result.Items.add(item);
        if ("OK".equals(item.Status)) {
            result.Processed++;
        } else if ("SKIPPED".equals(item.Status)) {
            result.Skipped++;
        } else {
            result.Errors++;
        }
    }

    private List<VideoEntity> resolveVideos(String videoCod) {
        if (hasText(videoCod)) {
            VideoEntity video = videoRepository.findById(videoCod)
                    .orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
            return List.of(video);
        }
        return videoRepository.findAll();
    }

    private double readDurationSeconds(Path source) throws Exception {
        CommandResult result = runCommand(List.of(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                source.toString()
        ));
        if (result.ExitCode() != 0) {
            throw new IllegalStateException("ffprobe fallo: " + result.Output());
        }
        return Double.parseDouble(result.Output().trim());
    }

    private ResolutionValue readResolution(Path source) throws Exception {
        CommandResult result = runCommand(List.of(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=s=x:p=0",
                source.toString()
        ));
        if (result.ExitCode() != 0) {
            throw new IllegalStateException("ffprobe fallo obteniendo resolucion: " + result.Output());
        }
        String output = result.Output().trim();
        String[] parts = output.split("x", 2);
        if (parts.length != 2) {
            throw new IllegalStateException("ffprobe no devolvio una resolucion valida: " + output);
        }
        return new ResolutionValue(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }

    private void captureThumbnail(Path source, Path target, double second) throws Exception {
        CommandResult result = runCommand(List.of(
                "ffmpeg",
                "-y",
                "-ss", String.format(Locale.US, "%.3f", second),
                "-i", source.toString(),
                "-frames:v", "1",
                "-q:v", "2",
                target.toString()
        ));
        if (result.ExitCode() != 0) {
            throw new IllegalStateException("ffmpeg fallo: " + result.Output());
        }
        if (!Files.exists(target) || Files.size(target) == 0) {
            throw new IllegalStateException("ffmpeg no genero la miniatura.");
        }
    }

    private CommandResult runCommand(List<String> command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("Tiempo maximo excedido ejecutando " + command.get(0) + ".");
        }
        return new CommandResult(process.exitValue(), String.join(System.lineSeparator(), lines));
    }

    private double validatePercentage(Double percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage es obligatorio.");
        }
        if (percentage <= 0 || percentage >= 100) {
            throw new IllegalArgumentException("Percentage debe ser mayor a 0 y menor a 100.");
        }
        return percentage;
    }

    private String resolveProcessMode(String mode, Boolean overwrite) {
        if (Boolean.TRUE.equals(overwrite)) {
            return "ALL";
        }
        String safeMode = mode == null || mode.isBlank() ? "MISSING_THUMBNAIL" : mode.trim().toUpperCase(Locale.ROOT);
        if (!List.of("ALL", "MISSING_THUMBNAIL").contains(safeMode)) {
            throw new IllegalArgumentException("Mode debe ser ALL o MISSING_THUMBNAIL.");
        }
        return safeMode;
    }

    private String formatDuration(double totalSeconds) {
        long secondsValue = Math.round(totalSeconds);
        Duration duration = Duration.ofSeconds(secondsValue);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    private boolean missingFileMetadata(VideoEntity video) {
        return video.FileSizeBytes == null || video.FileSizeBytes <= 0 || video.ResolutionWidth == null || video.ResolutionHeight == null;
    }

    private void fillFileMetadataItem(VideoMetadataProcessItemDto item, VideoEntity video) {
        item.FileSizeBytes = video.FileSizeBytes;
        item.FileSizeLabel = formatFileSize(video.FileSizeBytes);
        item.ResolutionWidth = video.ResolutionWidth;
        item.ResolutionHeight = video.ResolutionHeight;
        item.ResolutionLabel = formatResolution(video.ResolutionWidth, video.ResolutionHeight);
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "";
        }
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value = value / 1024;
            unitIndex++;
        }
        return String.format(Locale.US, value >= 10 ? "%.0f %s" : "%.1f %s", value, units[unitIndex]);
    }

    private String formatResolution(Integer width, Integer height) {
        if (width == null || height == null || width <= 0 || height <= 0) {
            return "";
        }
        return width + "x" + height;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private VideoMetadataProcessItemDto skipped(VideoMetadataProcessItemDto item, String message) {
        item.Status = "SKIPPED";
        item.Message = message;
        return item;
    }

    private VideoMetadataProcessItemDto error(VideoMetadataProcessItemDto item, String message) {
        item.Status = "ERROR";
        item.Message = message;
        return item;
    }

    private record CommandResult(Integer ExitCode, String Output) {}
    private record ResolutionValue(Integer Width, Integer Height) {}
}

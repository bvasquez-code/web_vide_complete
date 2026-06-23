package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.dto.VideoCaptureProcessResultDto;
import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import com.ccadmin.app.video.model.entity.VideoEntity;
import com.ccadmin.app.video.repository.VideoCaptureRepository;
import com.ccadmin.app.video.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class VideoCaptureService extends SessionService {
    private final VideoRepository videoRepository;
    private final VideoCaptureRepository videoCaptureRepository;
    private final Path capturePath = Path.of("uploads", "captures");
    private final Integer defaultCaptureCount = 20;

    public VideoCaptureService(VideoRepository videoRepository, VideoCaptureRepository videoCaptureRepository) {
        this.videoRepository = videoRepository;
        this.videoCaptureRepository = videoCaptureRepository;
    }

    @Transactional
    public VideoCaptureProcessResultDto generate(String videoCod, String baseUrl) {
        if (videoCod == null || videoCod.isBlank()) {
            throw new IllegalArgumentException("Codigo de video obligatorio.");
        }
        VideoEntity video = videoRepository.findById(videoCod)
                .orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        if (!"PATH".equals(video.SourceType)) {
            throw new IllegalArgumentException("Solo se pueden generar capturas para videos con SourceType PATH.");
        }

        try {
            Path source = Path.of(video.SourceValue).normalize();
            if (!Files.exists(source) || !Files.isRegularFile(source) || !Files.isReadable(source)) {
                throw new IllegalArgumentException("Archivo de video no encontrado o sin permiso de lectura.");
            }

            double totalSeconds = readDurationSeconds(source);
            if (!Double.isFinite(totalSeconds) || totalSeconds <= 0) {
                throw new IllegalArgumentException("No se pudo obtener una duracion valida.");
            }

            Files.createDirectories(capturePath);
            Path videoCapturePath = capturePath.resolve(buildCaptureFolderName(video)).normalize();
            if (!videoCapturePath.startsWith(capturePath.normalize())) {
                throw new IllegalArgumentException("Nombre de carpeta de capturas invalido.");
            }
            Files.createDirectories(videoCapturePath);
            deleteExistingCaptureFiles(video.VideoCod);
            videoCaptureRepository.deleteByVideoCod(video.VideoCod);
            List<VideoCaptureEntity> captures = new ArrayList<>();

            for (int index = 1; index <= defaultCaptureCount; index++) {
                double captureSecond = resolveCaptureSecond(totalSeconds, index, defaultCaptureCount);
                String fileName = video.VideoCod + "-capture-" + String.format("%02d", index) + "-" + System.currentTimeMillis() + ".jpg";
                Path target = videoCapturePath.resolve(fileName).normalize();
                captureImage(source, target, captureSecond);

                VideoCaptureEntity capture = new VideoCaptureEntity();
                capture.VideoCod = video.VideoCod;
                capture.ImageUrl = baseUrl + "/api/v1/public/captures/" + videoCapturePath.getFileName() + "/" + fileName;
                capture.CaptureSecond = BigDecimal.valueOf(captureSecond);
                capture.DisplayOrder = index;
                capture.addSessionCreate(getUserCod());
                captures.add(videoCaptureRepository.save(capture));
            }

            video.Duration = formatDuration(totalSeconds);
            video.addSessionModify(getUserCod());
            videoRepository.save(video);

            VideoCaptureProcessResultDto result = new VideoCaptureProcessResultDto();
            result.VideoCod = video.VideoCod;
            result.Duration = video.Duration;
            result.CaptureCount = captures.size();
            result.Captures = captures;
            return result;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    public Path findCapture(String fileName) {
        Path target = capturePath.resolve(fileName).normalize();
        if (!target.startsWith(capturePath.normalize())) {
            throw new IllegalArgumentException("Nombre de captura invalido.");
        }
        return target;
    }

    @Transactional
    public VideoCaptureEntity saveCaptureAtSecond(String videoCod, Double captureSecond, String baseUrl) throws Exception {
        if (videoCod == null || videoCod.isBlank()) {
            throw new IllegalArgumentException("Codigo de video obligatorio.");
        }
        if (captureSecond == null || !Double.isFinite(captureSecond) || captureSecond < 0) {
            throw new IllegalArgumentException("Segundo de captura invalido.");
        }

        VideoEntity video = videoRepository.findById(videoCod)
                .orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));
        if (!"PATH".equals(video.SourceType)) {
            throw new IllegalArgumentException("La captura por segundo solo esta disponible para videos con SourceType PATH.");
        }

        Path source = Path.of(video.SourceValue).normalize();
        if (!Files.exists(source) || !Files.isRegularFile(source) || !Files.isReadable(source)) {
            throw new IllegalArgumentException("Archivo de video no encontrado o sin permiso de lectura.");
        }

        double totalSeconds = readDurationSeconds(source);
        if (!Double.isFinite(totalSeconds) || totalSeconds <= 0) {
            throw new IllegalArgumentException("No se pudo obtener una duracion valida.");
        }

        double safeSecond = normalizeCaptureSecond(captureSecond, totalSeconds);
        BigDecimal storedSecond = BigDecimal.valueOf(safeSecond).setScale(3, RoundingMode.HALF_UP);
        for (VideoCaptureEntity capture : videoCaptureRepository.findActiveByVideoCod(video.VideoCod)) {
            if (capture.CaptureSecond != null && capture.CaptureSecond.setScale(3, RoundingMode.HALF_UP).compareTo(storedSecond) == 0) {
                return capture;
            }
        }

        Files.createDirectories(capturePath);
        Path videoCapturePath = capturePath.resolve(buildCaptureFolderName(video)).normalize();
        if (!videoCapturePath.startsWith(capturePath.normalize())) {
            throw new IllegalArgumentException("Nombre de carpeta de capturas invalido.");
        }
        Files.createDirectories(videoCapturePath);

        long captureMillis = storedSecond.multiply(BigDecimal.valueOf(1000)).longValue();
        String fileName = video.VideoCod + "-capture-" + captureMillis + "ms-" + System.currentTimeMillis() + ".jpg";
        Path target = videoCapturePath.resolve(fileName).normalize();
        captureImage(source, target, safeSecond);

        Integer nextOrder = videoCaptureRepository.findMaxDisplayOrder(video.VideoCod) + 1;
        VideoCaptureEntity capture = new VideoCaptureEntity();
        capture.VideoCod = video.VideoCod;
        capture.ImageUrl = baseUrl + "/api/v1/public/captures/" + videoCapturePath.getFileName() + "/" + fileName;
        capture.CaptureSecond = storedSecond;
        capture.DisplayOrder = nextOrder;
        capture.addSessionCreate(getUserCod());
        return videoCaptureRepository.save(capture);
    }

    @Transactional
    public VideoCaptureEntity saveManualCapture(String videoCod, MultipartFile file, String baseUrl) throws Exception {
        if (videoCod == null || videoCod.isBlank()) {
            throw new IllegalArgumentException("Codigo de video obligatorio.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Imagen obligatoria.");
        }
        VideoEntity video = videoRepository.findById(videoCod)
                .orElseThrow(() -> new IllegalArgumentException("Video no encontrado."));

        Files.createDirectories(capturePath);
        Path videoCapturePath = capturePath.resolve(buildCaptureFolderName(video)).normalize();
        if (!videoCapturePath.startsWith(capturePath.normalize())) {
            throw new IllegalArgumentException("Nombre de carpeta de capturas invalido.");
        }
        Files.createDirectories(videoCapturePath);

        String newHash = sha256(file.getBytes());
        for (VideoCaptureEntity capture : videoCaptureRepository.findActiveByVideoCod(video.VideoCod)) {
            Path existingPath = resolvePathFromCaptureUrl(capture.ImageUrl);
            if (existingPath != null && Files.exists(existingPath) && newHash.equals(sha256(Files.readAllBytes(existingPath)))) {
                return capture;
            }
        }

        Integer nextOrder = videoCaptureRepository.findMaxDisplayOrder(video.VideoCod) + 1;
        String fileName = video.VideoCod + "-capture-manual-" + String.format("%02d", nextOrder) + "-" + System.currentTimeMillis() + ".jpg";
        Path target = videoCapturePath.resolve(fileName).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        VideoCaptureEntity capture = new VideoCaptureEntity();
        capture.VideoCod = video.VideoCod;
        capture.ImageUrl = baseUrl + "/api/v1/public/captures/" + videoCapturePath.getFileName() + "/" + fileName;
        capture.CaptureSecond = BigDecimal.ZERO;
        capture.DisplayOrder = nextOrder;
        capture.addSessionCreate(getUserCod());
        return videoCaptureRepository.save(capture);
    }

    private void deleteExistingCaptureFiles(String videoCod) {
        for (VideoCaptureEntity capture : videoCaptureRepository.findActiveByVideoCod(videoCod)) {
            if (capture.ImageUrl == null || capture.ImageUrl.isBlank()) {
                continue;
            }
            try {
                Path target = resolvePathFromCaptureUrl(capture.ImageUrl);
                if (target != null && target.startsWith(capturePath.normalize())) {
                    Files.deleteIfExists(target);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Path resolvePathFromCaptureUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String marker = "/captures/";
        int markerIndex = imageUrl.indexOf(marker);
        if (markerIndex >= 0) {
            String relativePath = imageUrl.substring(markerIndex + marker.length());
            Path target = capturePath.resolve(relativePath).normalize();
            return target.startsWith(capturePath.normalize()) ? target : null;
        }
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path target = capturePath.resolve(fileName).normalize();
        return target.startsWith(capturePath.normalize()) ? target : null;
    }

    private String sha256(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }

    private String buildCaptureFolderName(VideoEntity video) {
        String title = video.Title == null ? "" : video.Title.trim();
        String normalizedTitle = title
                .replaceAll("[^A-Za-z0-9 ]", "")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_");
        if (normalizedTitle.isBlank()) {
            normalizedTitle = "SIN_TITULO";
        }
        return video.VideoCod + "_" + normalizedTitle;
    }

    private double resolveCaptureSecond(double totalSeconds, int index, int captureCount) {
        double interval = totalSeconds / captureCount;
        double second = interval * index;
        second = Math.min(second, Math.max(0.1, totalSeconds - 0.1));
        return Math.max(0.1, second);
    }

    private double normalizeCaptureSecond(double requestedSecond, double totalSeconds) {
        double maxSecond = Math.max(0.1, totalSeconds - 0.1);
        double second = Math.min(requestedSecond, maxSecond);
        second = Math.max(0.1, second);
        return BigDecimal.valueOf(second).setScale(3, RoundingMode.HALF_UP).doubleValue();
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

    private void captureImage(Path source, Path target, double second) throws Exception {
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
            throw new IllegalStateException("ffmpeg no genero la captura.");
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

    private record CommandResult(Integer ExitCode, String Output) {}
}

package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.dto.VideoCaptureCleanupResultDto;
import com.ccadmin.app.video.model.dto.VideoCaptureProcessResultDto;
import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import com.ccadmin.app.video.model.entity.VideoEntity;
import com.ccadmin.app.video.repository.VideoCaptureRepository;
import com.ccadmin.app.video.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class VideoCaptureService extends SessionService {
    private static final String CAPTURE_SOURCE_AUTO = "AUTO";
    private static final String CAPTURE_SOURCE_MANUAL = "MANUAL";

    private final VideoRepository videoRepository;
    private final VideoCaptureRepository videoCaptureRepository;
    private final TransactionTemplate transactionTemplate;
    private final Path capturePath = Path.of("uploads", "captures");
    private final Integer defaultCaptureCount = 20;

    public VideoCaptureService(VideoRepository videoRepository, VideoCaptureRepository videoCaptureRepository, PlatformTransactionManager transactionManager) {
        this.videoRepository = videoRepository;
        this.videoCaptureRepository = videoCaptureRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

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
            deleteExistingCaptureFiles(video.VideoCod, CAPTURE_SOURCE_AUTO);
            transactionTemplate.executeWithoutResult(status -> videoCaptureRepository.deleteByVideoCodAndCaptureSource(video.VideoCod, CAPTURE_SOURCE_AUTO));
            List<VideoCaptureEntity> captures = new ArrayList<>();

            for (int index = 1; index <= defaultCaptureCount; index++) {
                double captureSecond = resolveCaptureSecond(totalSeconds, index, defaultCaptureCount);
                String fileName = video.VideoCod + "-capture-" + String.format("%02d", index) + "-" + System.currentTimeMillis() + ".jpg";
                Path target = videoCapturePath.resolve(fileName).normalize();
                try {
                    captureImage(source, target, captureSecond);
                } catch (Exception ex) {
                    Files.deleteIfExists(target);
                    continue;
                }

                VideoCaptureEntity capture = new VideoCaptureEntity();
                capture.VideoCod = video.VideoCod;
                capture.ImageUrl = baseUrl + "/api/v1/public/captures/" + videoCapturePath.getFileName() + "/" + fileName;
                capture.CaptureSource = CAPTURE_SOURCE_AUTO;
                capture.CaptureSecond = BigDecimal.valueOf(captureSecond);
                capture.DisplayOrder = index;
                capture.addSessionCreate(getUserCod());
                VideoCaptureEntity savedCapture = transactionTemplate.execute(status -> videoCaptureRepository.save(capture));
                if (savedCapture != null) {
                    captures.add(savedCapture);
                }
            }

            video.Duration = formatDuration(totalSeconds);
            video.addSessionModify(getUserCod());
            transactionTemplate.executeWithoutResult(status -> videoRepository.save(video));

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

    public VideoCaptureCleanupResultDto cleanUnlinkedCaptures(Boolean dryRun) throws Exception {
        VideoCaptureCleanupResultDto result = new VideoCaptureCleanupResultDto();
        result.DryRun = Boolean.TRUE.equals(dryRun);
        Files.createDirectories(capturePath);

        Set<Path> linkedFiles = new HashSet<>();
        for (String imageUrl : videoCaptureRepository.findAllImageUrls()) {
            Path linkedPath = resolvePathFromCaptureUrl(imageUrl);
            if (linkedPath != null) {
                linkedFiles.add(linkedPath.toAbsolutePath().normalize());
            }
        }
        result.LinkedFileCount = linkedFiles.size();

        try (Stream<Path> paths = Files.walk(capturePath)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                result.ScannedFileCount++;
                Path normalizedFile = file.toAbsolutePath().normalize();
                if (linkedFiles.contains(normalizedFile)) {
                    continue;
                }

                String relativeFile = capturePath.toAbsolutePath().normalize().relativize(normalizedFile).toString();
                if (!result.DryRun) {
                    try {
                        Files.deleteIfExists(normalizedFile);
                    } catch (Exception ex) {
                        result.ErrorCount++;
                        result.Errors.add(relativeFile + ": " + ex.getMessage());
                        continue;
                    }
                }
                result.DeletedFileCount++;
                result.DeletedFiles.add(relativeFile);
            }
        }

        if (!result.DryRun) {
            deleteEmptyCaptureDirectories();
        }
        return result;
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
        for (VideoCaptureEntity capture : videoCaptureRepository.findActiveByVideoCodAndCaptureSource(video.VideoCod, CAPTURE_SOURCE_MANUAL)) {
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
        capture.CaptureSource = CAPTURE_SOURCE_MANUAL;
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
        capture.CaptureSource = CAPTURE_SOURCE_MANUAL;
        capture.CaptureSecond = BigDecimal.ZERO;
        capture.DisplayOrder = nextOrder;
        capture.addSessionCreate(getUserCod());
        return videoCaptureRepository.save(capture);
    }

    private void deleteExistingCaptureFiles(String videoCod, String captureSource) {
        for (VideoCaptureEntity capture : videoCaptureRepository.findActiveByVideoCodAndCaptureSource(videoCod, captureSource)) {
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

    private void deleteEmptyCaptureDirectories() throws Exception {
        if (!Files.exists(capturePath)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(capturePath)) {
            List<Path> directories = paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.normalize().equals(capturePath.normalize()))
                    .sorted(Comparator.reverseOrder())
                    .toList();
            for (Path directory : directories) {
                try (Stream<Path> children = Files.list(directory)) {
                    if (children.findAny().isEmpty()) {
                        Files.deleteIfExists(directory);
                    }
                }
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
        List<List<String>> commands = List.of(
                List.of(
                        "ffmpeg",
                        "-y",
                        "-ss", String.format(Locale.US, "%.3f", second),
                        "-i", source.toString(),
                        "-frames:v", "1",
                        "-q:v", "2",
                        target.toString()
                ),
                List.of(
                        "ffmpeg",
                        "-y",
                        "-ss", String.format(Locale.US, "%.3f", second),
                        "-i", source.toString(),
                        "-frames:v", "1",
                        "-vf", "format=yuvj420p",
                        "-q:v", "3",
                        "-update", "1",
                        target.toString()
                )
        );

        List<String> errors = new ArrayList<>();
        for (List<String> command : commands) {
            Files.deleteIfExists(target);
            CommandResult result = runCommand(command, 5, TimeUnit.SECONDS);
            if (result.ExitCode() == 0 && Files.exists(target) && Files.size(target) > 0) {
                return;
            }
            errors.add(result.Output());
        }
        throw new IllegalStateException("ffmpeg no genero la captura despues de 2 intentos: " + String.join(System.lineSeparator(), errors));
    }

    private CommandResult runCommand(List<String> command) throws Exception {
        return runCommand(command, 5, TimeUnit.MINUTES);
    }

    private CommandResult runCommand(List<String> command, long timeout, TimeUnit timeUnit) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (Exception ex) {
                lines.add(ex.getMessage());
            }
            return String.join(System.lineSeparator(), lines);
        });

        boolean finished = process.waitFor(timeout, timeUnit);
        if (!finished) {
            process.destroyForcibly();
            String output = readCommandOutput(outputFuture);
            return new CommandResult(-1, "Tiempo maximo excedido ejecutando " + command.get(0) + "." + System.lineSeparator() + output);
        }
        return new CommandResult(process.exitValue(), readCommandOutput(outputFuture));
    }

    private String readCommandOutput(CompletableFuture<String> outputFuture) {
        try {
            return outputFuture.get(1, TimeUnit.SECONDS);
        } catch (Exception ex) {
            return ex.getMessage();
        }
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

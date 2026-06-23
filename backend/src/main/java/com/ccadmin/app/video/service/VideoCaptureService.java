package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.dto.VideoCaptureProcessResultDto;
import com.ccadmin.app.video.model.entity.VideoCaptureEntity;
import com.ccadmin.app.video.model.entity.VideoEntity;
import com.ccadmin.app.video.repository.VideoCaptureRepository;
import com.ccadmin.app.video.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
            deleteExistingCaptureFiles(video.VideoCod);
            videoCaptureRepository.deleteByVideoCod(video.VideoCod);
            List<VideoCaptureEntity> captures = new ArrayList<>();

            for (int index = 1; index <= defaultCaptureCount; index++) {
                double captureSecond = resolveCaptureSecond(totalSeconds, index, defaultCaptureCount);
                String fileName = video.VideoCod + "-capture-" + String.format("%02d", index) + "-" + System.currentTimeMillis() + ".jpg";
                Path target = capturePath.resolve(fileName).normalize();
                captureImage(source, target, captureSecond);

                VideoCaptureEntity capture = new VideoCaptureEntity();
                capture.VideoCod = video.VideoCod;
                capture.ImageUrl = baseUrl + "/api/v1/public/captures/" + fileName;
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

    private void deleteExistingCaptureFiles(String videoCod) {
        for (VideoCaptureEntity capture : videoCaptureRepository.findActiveByVideoCod(videoCod)) {
            if (capture.ImageUrl == null || capture.ImageUrl.isBlank()) {
                continue;
            }
            String fileName = capture.ImageUrl.substring(capture.ImageUrl.lastIndexOf("/") + 1);
            try {
                Path target = capturePath.resolve(fileName).normalize();
                if (target.startsWith(capturePath.normalize())) {
                    Files.deleteIfExists(target);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private double resolveCaptureSecond(double totalSeconds, int index, int captureCount) {
        double interval = totalSeconds / captureCount;
        double second = interval * index;
        second = Math.min(second, Math.max(0.1, totalSeconds - 0.1));
        return Math.max(0.1, second);
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

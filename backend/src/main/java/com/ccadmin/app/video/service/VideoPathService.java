package com.ccadmin.app.video.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class VideoPathService {
    @Value("${app.video.source-base-path:}")
    private String sourceBasePathValue;

    private Path workingDirectory;
    private Path projectRoot;
    private Path sourceBasePath;

    @PostConstruct
    public void init() {
        workingDirectory = Path.of("").toAbsolutePath().normalize();
        projectRoot = findProjectRoot(workingDirectory);
        sourceBasePath = resolveBasePath(sourceBasePathValue);
    }

    public Path resolveSourcePath(String sourceValue) {
        if (sourceValue == null || sourceValue.isBlank()) {
            throw new IllegalArgumentException("El video no tiene ruta de archivo registrada.");
        }

        Path rawPath = Path.of(sourceValue.trim()).normalize();
        if (rawPath.isAbsolute()) {
            return rawPath;
        }

        List<Path> candidates = new ArrayList<>();
        if (sourceBasePath != null) {
            candidates.add(sourceBasePath.resolve(rawPath).normalize());
        }
        candidates.add(workingDirectory.resolve(rawPath).normalize());
        if (workingDirectory.getParent() != null) {
            candidates.add(workingDirectory.getParent().resolve(rawPath).normalize());
        }
        if (projectRoot != null) {
            candidates.add(projectRoot.resolve(rawPath).normalize());
        }

        Set<Path> uniqueCandidates = new LinkedHashSet<>(candidates);
        for (Path candidate : uniqueCandidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return uniqueCandidates.iterator().next();
    }

    public String renamedSourceValue(String originalSourceValue, Path renamedPath) {
        Path rawPath = Path.of(originalSourceValue.trim()).normalize();
        if (rawPath.isAbsolute()) {
            return renamedPath.toString();
        }

        Path parent = rawPath.getParent();
        Path newFileName = renamedPath.getFileName();
        if (parent == null) {
            return newFileName.toString();
        }
        return parent.resolve(newFileName).normalize().toString();
    }

    private Path resolveBasePath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Path rawPath = Path.of(value.trim()).normalize();
        if (rawPath.isAbsolute()) {
            return rawPath;
        }
        if (projectRoot != null) {
            return projectRoot.resolve(rawPath).normalize();
        }
        return workingDirectory.resolve(rawPath).normalize();
    }

    private Path findProjectRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.isDirectory(current.resolve("backend"))
                    && Files.isDirectory(current.resolve("frontend"))
                    && Files.isDirectory(current.resolve("database"))) {
                return current;
            }
            current = current.getParent();
        }
        return start;
    }
}

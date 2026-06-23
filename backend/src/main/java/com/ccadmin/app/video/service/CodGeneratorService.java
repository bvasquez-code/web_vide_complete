package com.ccadmin.app.video.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class CodGeneratorService {
    public String next(String prefix) {
        String safePrefix = prefix == null ? "" : prefix.toUpperCase();
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        int availableLength = Math.max(0, 16 - safePrefix.length());
        return (safePrefix + uuid.substring(0, availableLength)).substring(0, 16);
    }
}

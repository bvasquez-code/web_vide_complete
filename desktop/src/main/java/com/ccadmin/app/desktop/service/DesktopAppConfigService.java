package com.ccadmin.app.desktop.service;

import com.ccadmin.app.desktop.model.entity.DesktopAppConfigEntity;
import com.ccadmin.app.desktop.repository.DesktopAppConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class DesktopAppConfigService {
    public static final String UPLOAD_ROOT = "UPLOAD_ROOT";
    private final DesktopAppConfigRepository repository;

    public DesktopAppConfigService(DesktopAppConfigRepository repository) {
        this.repository = repository;
    }

    public String getUploadRoot() {
        return repository.findById(UPLOAD_ROOT)
                .map(config -> config.ConfigValue)
                .filter(value -> !value.isBlank())
                .orElse("uploads");
    }

    public DesktopAppConfigEntity saveUploadRoot(String uploadRoot) {
        if (uploadRoot == null || uploadRoot.isBlank()) {
            throw new IllegalArgumentException("La ruta de uploads es obligatoria.");
        }
        DesktopAppConfigEntity entity = repository.findById(UPLOAD_ROOT).orElseGet(DesktopAppConfigEntity::new);
        entity.ConfigKey = UPLOAD_ROOT;
        entity.ConfigValue = uploadRoot.trim();
        entity.Description = "Ruta base local de uploads para miniaturas y capturas en escritorio.";
        if (entity.CreationUser == null || entity.CreationUser.isBlank()) {
            entity.addSessionCreate("SISTEMA");
        } else {
            entity.addSessionModify("SISTEMA");
        }
        return repository.save(entity);
    }
}

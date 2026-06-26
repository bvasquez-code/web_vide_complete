package com.ccadmin.app.desktop.repository;

import com.ccadmin.app.desktop.model.entity.DesktopAppConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesktopAppConfigRepository extends JpaRepository<DesktopAppConfigEntity, String> {
}

package com.ccadmin.app.admin.service;

import com.ccadmin.app.admin.model.dto.LoginDto;
import com.ccadmin.app.admin.model.dto.LoginResponseDto;
import com.ccadmin.app.admin.model.entity.AdminUserEntity;
import com.ccadmin.app.admin.repository.AdminUserRepository;
import com.ccadmin.app.security.service.TokenUtil;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class AdminAuthService {
    private final AdminUserRepository adminUserRepository;
    private final TokenUtil tokenUtil;

    public AdminAuthService(AdminUserRepository adminUserRepository, TokenUtil tokenUtil) {
        this.adminUserRepository = adminUserRepository;
        this.tokenUtil = tokenUtil;
    }

    public LoginResponseDto login(LoginDto dto) {
        if (dto == null || dto.UserName == null || dto.UserName.isBlank() || dto.Password == null || dto.Password.isBlank()) {
            throw new IllegalArgumentException("Usuario y contrasena son obligatorios.");
        }
        AdminUserEntity user = adminUserRepository.findByUserNameAndStatus(dto.UserName, "A")
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas."));
        if (!sha256(dto.Password).equalsIgnoreCase(user.PasswordHash)) {
            throw new IllegalArgumentException("Credenciales invalidas.");
        }
        return new LoginResponseDto(tokenUtil.createToken(user.AdminUserCod), user.AdminUserCod, user.UserName, user.Names);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo validar la contrasena.", ex);
        }
    }
}

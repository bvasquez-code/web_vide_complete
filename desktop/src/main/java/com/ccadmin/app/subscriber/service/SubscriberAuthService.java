package com.ccadmin.app.subscriber.service;

import com.ccadmin.app.security.service.TokenUtil;
import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.subscriber.model.dto.SubscriberLoginDto;
import com.ccadmin.app.subscriber.model.dto.SubscriberLoginResponseDto;
import com.ccadmin.app.subscriber.model.dto.SubscriberRegisterDto;
import com.ccadmin.app.subscriber.model.entity.SubscriberUserEntity;
import com.ccadmin.app.subscriber.repository.SubscriberUserRepository;
import com.ccadmin.app.video.service.CodGeneratorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class SubscriberAuthService extends SessionService {
    private final SubscriberUserRepository subscriberUserRepository;
    private final CodGeneratorService codGeneratorService;
    private final TokenUtil tokenUtil;

    public SubscriberAuthService(SubscriberUserRepository subscriberUserRepository, CodGeneratorService codGeneratorService, TokenUtil tokenUtil) {
        this.subscriberUserRepository = subscriberUserRepository;
        this.codGeneratorService = codGeneratorService;
        this.tokenUtil = tokenUtil;
    }

    @Transactional
    public SubscriberLoginResponseDto register(SubscriberRegisterDto dto) {
        if (dto == null || isBlank(dto.UserName) || isBlank(dto.Email) || isBlank(dto.Password)) {
            throw new IllegalArgumentException("Usuario, correo y contrasena son obligatorios.");
        }
        if (dto.Password.length() < 6) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres.");
        }
        if (subscriberUserRepository.countByUserNameOrEmail(dto.UserName.trim(), dto.Email.trim()) > 0) {
            throw new IllegalArgumentException("El usuario o correo ya esta registrado.");
        }

        SubscriberUserEntity user = new SubscriberUserEntity();
        user.SubscriberUserCod = codGeneratorService.next("SUB");
        user.UserName = dto.UserName.trim();
        user.Email = dto.Email.trim();
        user.Names = dto.Names == null ? "" : dto.Names.trim();
        user.PasswordHash = sha256(dto.Password);
        user.addSessionCreate(user.SubscriberUserCod);
        subscriberUserRepository.save(user);
        return toLoginResponse(user);
    }

    @Transactional
    public SubscriberLoginResponseDto login(SubscriberLoginDto dto) {
        if (dto == null || isBlank(dto.UserName) || isBlank(dto.Password)) {
            throw new IllegalArgumentException("Usuario y contrasena son obligatorios.");
        }
        SubscriberUserEntity user = subscriberUserRepository.findByLoginAndStatus(dto.UserName.trim(), "A")
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas."));
        if (!sha256(dto.Password).equalsIgnoreCase(user.PasswordHash)) {
            throw new IllegalArgumentException("Credenciales invalidas.");
        }
        user.LastLoginDate = LocalDateTime.now();
        user.addSessionModify(user.SubscriberUserCod);
        subscriberUserRepository.save(user);
        return toLoginResponse(user);
    }

    public SubscriberUserEntity me() {
        return subscriberUserRepository.findById(requireViewer()).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    public String requireViewer() {
        String userCod = getUserCod();
        if (userCod == null || !userCod.startsWith("SUB")) {
            throw new IllegalArgumentException("Debe iniciar sesion como usuario visualizador.");
        }
        return userCod;
    }

    private SubscriberLoginResponseDto toLoginResponse(SubscriberUserEntity user) {
        return new SubscriberLoginResponseDto(tokenUtil.createToken(user.SubscriberUserCod, "VIEWER"), user.SubscriberUserCod, user.UserName, user.Names, user.Email);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

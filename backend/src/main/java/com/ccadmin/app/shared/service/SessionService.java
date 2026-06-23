package com.ccadmin.app.shared.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SessionService {
    protected String getUserCod() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return "SISTEMA";
        }
        return auth.getName();
    }
}

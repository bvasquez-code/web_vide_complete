package com.ccadmin.app.desktop.ui;

import com.ccadmin.app.shared.model.dto.ResponseWsDto;
import org.springframework.http.ResponseEntity;

public final class DesktopResponse {
    private DesktopResponse() {
    }

    public static Object data(ResponseEntity<ResponseWsDto> response) {
        ResponseWsDto body = response == null ? null : response.getBody();
        if (body == null) {
            throw new IllegalStateException("La operacion no devolvio respuesta.");
        }
        if (Boolean.TRUE.equals(body.ErrorStatus)) {
            throw new IllegalStateException(body.Message);
        }
        return body.Data;
    }
}

package com.ccadmin.app.shared.model.dto;

import java.util.HashMap;
import java.util.Map;

public class ResponseWsDto {
    public String Status;
    public String Message;
    public Object Data;
    public Boolean ErrorStatus;
    public String ErrorID;
    public Map<String, Object> DataAdditional;

    public ResponseWsDto() {
        this.Status = "OK";
        this.Message = "OK";
        this.ErrorStatus = false;
        this.DataAdditional = new HashMap<>();
    }

    public ResponseWsDto(Object Data) {
        this();
        this.Data = Data;
    }

    public ResponseWsDto(Exception ex) {
        this.Status = "ERROR";
        this.Message = ex.getMessage();
        this.ErrorStatus = true;
        this.ErrorID = ex.getClass().getSimpleName();
        this.DataAdditional = new HashMap<>();
        this.Data = ex;
    }

    public ResponseWsDto okResponse(Object Data) {
        this.Data = Data;
        return this;
    }

    public ResponseWsDto AddResponseAdditional(String name, Object data) {
        this.DataAdditional.put(name, data);
        return this;
    }
}

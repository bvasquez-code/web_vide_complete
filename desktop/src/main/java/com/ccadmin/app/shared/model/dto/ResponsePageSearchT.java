package com.ccadmin.app.shared.model.dto;

import java.util.List;

public class ResponsePageSearchT<T> {
    public List<T> Data;
    public Long TotalRows;
    public Integer Page;
    public Integer Limit;

    public ResponsePageSearchT(List<T> data, Long totalRows, Integer page, Integer limit) {
        this.Data = data;
        this.TotalRows = totalRows;
        this.Page = page;
        this.Limit = limit;
    }
}

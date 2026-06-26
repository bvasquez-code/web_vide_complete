package com.ccadmin.app.shared.model.dto;

public class SearchDto {
    public String Query = "";
    public Integer Page = 1;
    public Integer Limit = 10;
    public Integer Init = 0;

    public Integer getInit() {
        Integer safePage = Page == null || Page < 1 ? 1 : Page;
        Integer safeLimit = Limit == null || Limit < 1 ? 10 : Limit;
        return (safePage - 1) * safeLimit;
    }
}

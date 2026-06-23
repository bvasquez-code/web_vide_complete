package com.ccadmin.app.admin.model.dto;

public class LoginResponseDto {
    public String Token;
    public String UserCod;
    public String UserName;
    public String Names;

    public LoginResponseDto(String token, String userCod, String userName, String names) {
        this.Token = token;
        this.UserCod = userCod;
        this.UserName = userName;
        this.Names = names;
    }
}

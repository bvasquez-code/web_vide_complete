package com.ccadmin.app.subscriber.model.dto;

public class SubscriberLoginResponseDto {
    public String Token;
    public String UserCod;
    public String UserName;
    public String Names;
    public String Email;

    public SubscriberLoginResponseDto(String token, String userCod, String userName, String names, String email) {
        this.Token = token;
        this.UserCod = userCod;
        this.UserName = userName;
        this.Names = names;
        this.Email = email;
    }
}

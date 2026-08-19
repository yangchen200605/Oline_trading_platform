package com.atguigu.oline_trading_platform.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    private String oldPassword;
    private String newPassword;
}

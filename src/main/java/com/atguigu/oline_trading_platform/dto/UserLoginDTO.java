package com.atguigu.oline_trading_platform.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {
    /** 简化登录：直接传 openid（真实项目用微信 code 换 openid） */
    private String openid;
}

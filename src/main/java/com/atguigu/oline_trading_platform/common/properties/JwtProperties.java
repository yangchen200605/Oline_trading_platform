package com.atguigu.oline_trading_platform.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oline.jwt")
public class JwtProperties {

    private String adminSecretKey;
    private Long adminTtl;
    private String adminTokenName;

    private String userSecretKey;
    private Long userTtl;
    private String userTokenName;
}

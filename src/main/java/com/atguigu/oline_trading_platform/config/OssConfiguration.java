package com.atguigu.oline_trading_platform.config;

import com.atguigu.oline_trading_platform.common.properties.AliOssProperties;
import com.atguigu.oline_trading_platform.common.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OssConfiguration {

    private final AliOssProperties aliOssProperties;

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil() {
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}

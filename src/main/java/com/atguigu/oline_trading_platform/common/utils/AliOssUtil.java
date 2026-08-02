package com.atguigu.oline_trading_platform.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(byte[] bytes, ObjectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } finally {
            ossClient.shutdown();
        }
        // endpoint 形如 https://oss-cn-hangzhou.aliyuncs.com
        String host = endpoint.replace("https://", "").replace("http://", "");
        return "https://" + bucketName + "." + host + "/" + objectName;
    }

    public boolean isConfigured() {
        return accessKeyId != null
                && !accessKeyId.isBlank()
                && !"your-access-key-id".equals(accessKeyId);
    }
}

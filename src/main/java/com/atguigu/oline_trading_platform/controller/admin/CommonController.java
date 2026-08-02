package com.atguigu.oline_trading_platform.controller.admin;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
@RequiredArgsConstructor
public class CommonController {

    private final AliOssUtil aliOssUtil;

    @Value("${server.port:8080}")
    private int serverPort;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String objectName = UUID.randomUUID() + ext;

            if (aliOssUtil.isConfigured()) {
                String url = aliOssUtil.upload(file.getBytes(), objectName);
                return Result.success(url);
            }

            // 未配置真实 OSS 时落到本地，方便联调
            Path dir = Paths.get("uploads");
            Files.createDirectories(dir);
            Path target = dir.resolve(objectName);
            file.transferTo(target.toFile());
            String url = "http://localhost:" + serverPort + "/uploads/" + objectName;
            log.warn("OSS 未配置，文件已保存到本地: {}", url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }
}

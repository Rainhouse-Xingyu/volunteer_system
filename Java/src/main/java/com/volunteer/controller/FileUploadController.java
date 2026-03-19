package com.volunteer.controller;

import com.volunteer.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    // 绑定配置文件中的上传路径，默认当前项目下的 uploads 目录
    @Value("${file.upload-dir:./uploads/}")
    private String uploadDir;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        // 1. 创建上传目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 生成文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;

        // 3. 保存文件
        try {
            File dest = new File(dir.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);
            
            // 返回访问路径 (需配合 WebConfig 资源映射)
            // 假设映射路径为 /uploads/** -> file:./uploads/
            // 如果 server.servlet.context-path 配置了 /api，则返回 /api/uploads/xxx
            String path = "/uploads/" + newFilename;
            String cp = contextPath;
            if (cp != null && !cp.isEmpty()) {
                // Remove trailing slash if exists to avoid double slash
                if (cp.endsWith("/")) {
                    cp = cp.substring(0, cp.length() - 1);
                }
                path = cp + path;
            }
            return Result.success(path);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败");
        }
    }
}

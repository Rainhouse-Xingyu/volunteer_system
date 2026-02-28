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
            // 返回全路径或者相对路径，这里返回相对路径供前端拼接
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败");
        }
    }
}

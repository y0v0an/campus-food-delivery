package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    // 允许的文件类型
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    
    // 最大文件大小 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private Path getUploadPath() {
        // 使用相对路径，存储在cfgs项目的img目录下
        String userDir = System.getProperty("user.dir");
        Path uploadPath;
        
        // 判断当前运行目录，确保存储到cfgs/img下
        if (userDir.endsWith("cfgs")) {
            uploadPath = Paths.get(userDir, "img");
        } else {
            uploadPath = Paths.get(userDir, "cfgs", "img");
        }
        
        // 确保目录存在
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadPath, e);
        }
        return uploadPath;
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过5MB");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名无效");
        }

        // 获取文件扩展名
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = originalFilename.substring(dotIndex + 1).toLowerCase();
        }
        
        // 验证文件类型
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error("不支持的文件类型，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 生成新文件名（UUID确保唯一性）
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 保存文件
        Path destFile = getUploadPath().resolve(newFilename);
        try {
            file.transferTo(destFile.toFile());
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }

        // 返回文件访问路径
        Map<String, String> data = new HashMap<>();
        data.put("filename", newFilename);
        data.put("url", "/img/" + newFilename);

        return Result.success(data);
    }
}

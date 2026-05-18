package com.community.cfgs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用相对路径，映射cfgs项目的img目录
        String userDir = System.getProperty("user.dir");
        Path uploadPath;
        
        // 判断当前运行目录，确保映射到cfgs/img
        if (userDir.endsWith("cfgs")) {
            uploadPath = Paths.get(userDir, "img");
        } else {
            uploadPath = Paths.get(userDir, "cfgs", "img");
        }
        
        // 配置静态资源映射，让 /img/** 可以访问上传的图片
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + uploadPath.toAbsolutePath().toString() + "/");
    }
}

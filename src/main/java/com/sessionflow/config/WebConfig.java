package com.sessionflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 處理前端靜態資源
        registry.addResourceHandler("/sessionflowapp/**")
                .addResourceLocations("classpath:/static/sessionflowapp/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 如果請求的資源存在，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // 對於 SPA 路由，返回 index.html
                        if (resourcePath.startsWith("assets/") || resourcePath.equals("favicon.ico")) {
                            return super.getResource(resourcePath, location);
                        }
                        return new ClassPathResource("/static/sessionflowapp/index.html");
                    }
                });
        
        // 處理根路徑和其他前端路由
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/sessionflowapp/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // API 路徑不處理
                        if (resourcePath.startsWith("api/") || 
                            resourcePath.startsWith("swagger-ui") || 
                            resourcePath.startsWith("h2-console") ||
                            resourcePath.startsWith("ws") ||
                            resourcePath.startsWith("actuator/")) {
                            return null;
                        }
                        
                        // 如果是靜態資源文件且存在，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // 對於 SPA 路由，返回 index.html
                        return new ClassPathResource("/static/sessionflowapp/index.html");
                    }
                });
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 將根路徑重定向到前端應用
        registry.addViewController("/").setViewName("forward:/sessionflowapp/index.html");
        // 處理 /sessionflowapp 和 /sessionflowapp/ 路徑
        registry.addViewController("/sessionflowapp").setViewName("forward:/sessionflowapp/index.html");
        registry.addViewController("/sessionflowapp/").setViewName("forward:/sessionflowapp/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允許前端應用呼叫 API
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
} 
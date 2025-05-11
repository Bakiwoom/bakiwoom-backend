package com.javaex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.lang.NonNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("http://localhost:3000", "http://localhost:8000", "http://3.38.213.53:8082") // 프론트엔드와 AI 서버 허용
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH")
			.allowedHeaders("*")
			.exposedHeaders("Authorization")
			.allowCredentials(true);
	}

	@Override
	public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
		String saveDir;
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("linux")) {
			System.out.println("리눅스");
			saveDir = "/app/upload/";
		} else {
			System.out.println("윈도우");
			saveDir = "C:\\javaStudy\\upload\\";
		}

		registry.addResourceHandler("/upload/**").addResourceLocations("file:" + saveDir);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
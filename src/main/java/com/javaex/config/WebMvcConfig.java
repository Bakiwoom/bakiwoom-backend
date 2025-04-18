package com.javaex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/**")
	        .allowedOriginPatterns("*","http://localhost:3000","https://yummmy.shop") // 모든 출처 허용 (특정 패턴 사용 가11능)
	        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
	        .allowedHeaders("*")
	        .exposedHeaders("Authorization")
	        .allowCredentials(true);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		String saveDir;
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("linux")) {
			System.out.println("리눅스");
			// saveDir = "/home/ec2-user/upload/";
			saveDir = "/app/upload/";

		} else {
			System.out.println("윈도우");
			saveDir = "C:\\javaStudy\\upload\\";
		}

		registry.addResourceHandler("/upload/**").addResourceLocations("file:" + saveDir);
	}

}
package com.javaex.config;  // 네가 사용 중인 패키지 경로에 맞게 설정

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AmazonS3Config {

    @Value("${cloud.aws.credentials.accessKey}")  // application.properties에 설정된 값을 불러옴
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")  // application.properties에 설정된 값을 불러옴1
    private String secretKey;

    @Value("${cloud.aws.region.static}")  // 리전 정보
    private String region;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
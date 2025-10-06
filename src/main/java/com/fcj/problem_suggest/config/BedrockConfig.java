package com.fcj.problem_suggest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.time.Duration;

@Configuration
public class BedrockConfig {
    
    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.access-key-id}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public BedrockRuntimeClient bedrockClient() {
        return BedrockRuntimeClient.builder()
            .region(Region.of(region))
            .httpClient(ApacheHttpClient.builder()
                .socketTimeout(Duration.ofSeconds(100))
                .connectionTimeout(Duration.ofSeconds(100))
                .build())
            .overrideConfiguration(
                ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofSeconds(100))
                    .apiCallAttemptTimeout(Duration.ofSeconds(100))
                    .build())
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .build();
    }
}
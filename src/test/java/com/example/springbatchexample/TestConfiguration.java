package com.example.springbatchexample;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing      // 배치 작업 관리를 위한 JobBuilderFactory, StepBuilderFactory 등의 빈 자동 구성
@EnableAutoConfiguration
public class TestConfiguration {

}

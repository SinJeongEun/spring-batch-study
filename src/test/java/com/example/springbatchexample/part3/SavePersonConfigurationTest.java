package com.example.springbatchexample.part3;

import com.example.springbatchexample.TestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBatchTest
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class })
public class SavePersonConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void test_allow_duplicate() throws Exception {
        // job parameters 설정
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allow_duplicate", "false")
                .toJobParameters();

        // SavePersonConfiguration.class 에서 실행된 job 실행결과가 리턴된다.
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 검증
        Assertions.assertThat(jobExecution.getStepExecutions().stream() // 구조에 따라 N개의 StepExecution 리턴된다.
                .mapToInt(StepExecution::getWriteCount)
                .sum())
                .isEqualTo(3);
    }
}

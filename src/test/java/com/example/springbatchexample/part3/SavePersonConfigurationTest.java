package com.example.springbatchexample.part3;

import com.example.springbatchexample.TestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class })
public class SavePersonConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PersonRepository personRepository;

    @Before
    public void resetPerson(){
        personRepository.deleteAll();
    }

    @DisplayName("person 중복 insert 불가")
    @Test
    public void test_not_allow_duplicate() throws Exception {
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
                .isEqualTo(personRepository.count())
                .isEqualTo(3);
    }

    @DisplayName("person 중복 insert 허용")
    @Test
    public void test_allow_duplicate() throws Exception {
        // job parameters 설정
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allow_duplicate", "true")
                .toJobParameters();

        // SavePersonConfiguration.class 에서 실행된 job 실행결과가 리턴된다.
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 검증
        Assertions.assertThat(jobExecution.getStepExecutions().stream() // 구조에 따라 N개의 StepExecution 리턴된다.
                        .mapToInt(StepExecution::getWriteCount)
                        .sum())
                .isEqualTo(personRepository.count())
                .isEqualTo(100);
    }

    @DisplayName("Step 단위 테스트 (launchStep)")
    @Test
    public void test_step(){
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("savePersonStep");

        Assertions.assertThat(jobExecution.getStepExecutions().stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum())
                .isEqualTo(personRepository.count())
                .isEqualTo(3);
    }
}

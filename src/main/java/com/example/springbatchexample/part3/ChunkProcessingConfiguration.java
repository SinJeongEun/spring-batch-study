package com.example.springbatchexample.part3;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class ChunkProcessingConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public ChunkProcessingConfiguration(JobBuilderFactory jobBuilderFactory,
                                        StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job chunkProcessingJob() {
        return jobBuilderFactory.get("chunkProcessingJob")
                .incrementer(new RunIdIncrementer()) // 각 배치 job 실행마다 새로운 Run ID가 생성되어 JobParameters에 추가 (고유성 보장)
                .start(this.taskBaseStep()) // 17ms 소요
                .next(this.chunkBaseStep(null)) // 8ms 소요
                .build();
    }

    @Bean
    @JobScope
    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {

        return stepBuilderFactory.get("chunkBaseStep")
                .<String, String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10)// <input타입,output타입>이다.  , N개의 데이터를 [10개]씩 나눈다 -> 배치 대상을 일정 크기로 쪼갬.
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemReader<String> itemReader() {
        return new ListItemReader<>(getItems()); // 리스트를 읽는다
    }

    private ItemWriter<String> itemWriter() {
        // itemWriter은 itemReader. itemProcessor와 달리 일괄처리 한다.
        return items -> log.info("chunk item size : {}", items.size());
//        return items -> items.forEach(log::info);
    }

    private ItemProcessor<String, String> itemProcessor() { // itemReader에서 생성된 데이터를 가공하거나, witer로 넘길지 말지 결정한다 null을 리턴하면 넘어가지 않는다. ㄷ
        //즉 이 경우는 writer 로 넘어간다.
        // 만약 return null 인 경우에는 writer로 넘어가지 못하게 된다.
        return item -> item + ", Spring Batch";
    }

    @Bean
    public Step taskBaseStep() {
        return stepBuilderFactory.get("taskBaseStep")
                .tasklet(this.tasklet(null))
                .build();
    }

    @Bean
    @StepScope
    // tasklet 을 chunk처럼 사용하기
    public Tasklet tasklet(@Value("#{jobParameters[chunkSize]}") String value) { // chunk 처럼 처리 가능하지만 보시다시피 복잡하다
        List<String> items = getItems();

        return (contribution, chunkContext) -> {
            StepExecution stepExecution = contribution.getStepExecution();
            JobParameters jobParameters = stepExecution.getJobParameters(); // JobParameters 객체를 사용하여 job parameter 꺼내기
//            String value = jobParameters.getString("chunkSize", "10");

            int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10; // 이는 Spring Expression Language  방식

            int fromIndex = stepExecution.getReadCount(); // 0 , 10 ,,,
            int toIndex = fromIndex + chunkSize; // 시작 인덱스부터 Chunk 사이까지 읽은 인덱스 뽑기 / 0 + 10

            if (fromIndex >= items.size()) {
                return RepeatStatus.FINISHED;
            }

            List<String> subList = items.subList(fromIndex, toIndex); // 위의 계산으로 이 식을 사용하면 paging 효과/ 0 ~ 9 까지

            log.info("task item size : {}", subList.size());

            stepExecution.setReadCount(toIndex);

            return RepeatStatus.CONTINUABLE; // 이 tasklet 을 반복적으로 수행하라는 의미이다.
        };
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            items.add(i + " Hello");
        }

        return items;
    }

}

package com.example.springbatchexample.part3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaCursorItemReaderConfiguration {
    @Autowired
    private final JobBuilderFactory jobBuilderFactory;
    @Autowired
    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job itemReaderJob() throws Exception {
        return jobBuilderFactory.get("itemReaderJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaStep())
                .build();
    }

    @Bean
    public Step jpaStep() throws Exception {
        return stepBuilderFactory.get("jpaStep")
                .<Person,Person>chunk(10)
                .reader(jpaPagingItemReader())
                .writer(itemWriter())
                .build();
    }

    private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
        JpaCursorItemReader<Person> itemReader = new JpaCursorItemReaderBuilder<Person>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select p from Person p") // jpql 쿼리로 입력해야 됨
                .build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

    private JpaPagingItemReader<Person> jpaPagingItemReader() {
        JpaPagingItemReader itemReader = new JpaPagingItemReaderBuilder<Person>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(5)
                .queryString("select p from Person p")
                .parameterValues(new HashMap<String,Object>())
                .build();
        return itemReader;
    }

    private ItemWriter<Person> itemWriter() {
        return items -> log.info(items.stream()
                .map(Person::getName)
                .collect(Collectors.joining(", ")));
    }

}

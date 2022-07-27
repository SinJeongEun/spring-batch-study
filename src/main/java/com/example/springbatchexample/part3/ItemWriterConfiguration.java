package com.example.springbatchexample.part3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class ItemWriterConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Qualifier("datasource")
    private  DataSource dataSource;

    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource DataSource() {
       return DataSourceBuilder.create().build();
    }
    public ItemWriterConfiguration(JobBuilderFactory jobBuilderFactory,
                                       StepBuilderFactory stepBuilderFactory
//                                       DataSource dataSource
    ) {
            this.jobBuilderFactory = jobBuilderFactory;
            this.stepBuilderFactory = stepBuilderFactory;
//            this.dataSource = dataSource;
        }

    @Bean
    public Job itemWriterJob() throws Exception {
        return jobBuilderFactory.get("itemWriterJob")
                .incrementer(new RunIdIncrementer())
                .start(this.csvItemWriterStep())
                .next(this.jdbcBatchItemWriterStep())
                .build();
    }

    @Bean
    public Step csvItemWriterStep() throws Exception {

        return stepBuilderFactory.get("csvItemWriterStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .writer(csvItemWriter())
                .build();
    }

    @Bean
    public Step jdbcBatchItemWriterStep() {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    private ItemWriter<Person> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Person> itemWriter = new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>()) // BeanPropertyItemSqlParameterSourceProvider : person 객체를 파라미터로 자동생성 할 수 있는 기능
                .sql("insert into person(name, age, address) values(:name, :age, :address)")
                .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    private ItemWriter<Person> csvItemWriter() throws Exception {
        //----------person 으로 매핑하는 설정
        //csv 파일에 작성할 데이터를 추출하기 위해서 FiledExtracrot 객체가 필요하다.
        BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();

        //person 객체 기준으로 필드명을 설정한다.
        fieldExtractor.setNames(new String[]{"id", "name", "age", "address"});

        //각 필드의 데이터를 하나의 라인에 작성하기 위해 구분값을 설정한다.
        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        //---------writer 생성하기
        FlatFileItemWriter<Person> itemWriter = new FlatFileItemWriterBuilder<Person>()
                .name("csvItemWriter")
                .encoding("UTF-8")
                .resource(new FileSystemResource("output/test-output.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("id,이름,나이,거주지")) // csv 파일에 헤더 작성하기
                .footerCallback(writer -> writer.write("-------------------\n")) // csv 파일에 footer 작성하기
                .append(true) // 이 설정을 해야 기존 output/test-output.csv 파일에 덮어쓰지 않고, 뒤에 이어서 작성된다.
                .build();

        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    private ItemReader<Person> itemReader() {
        return new CustomItemReader<>(getItems());
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            items.add(new Person(i+1,"test name "+i ,"test age","test address"));
        }
        return items;
    }
}
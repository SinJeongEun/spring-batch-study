package com.example.springbatchexample.part3;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person> {

    private final RetryTemplate retryTemplate;

    public PersonValidationRetryProcessor() {
        this.retryTemplate = new RetryTemplateBuilder()
                .maxAttempts(2)
                .retryOn(NotFoundNameException.class)
                .build();
    }

    @Override
    public Person process(Person item) throws Exception {
        return this.retryTemplate.execute(context -> {
            //retryCallBack : 이 구간이 2번 재시도 된다
            if(item.isNotEmptyName()) return item;

            throw new NotFoundNameException();
        }, context -> {
            // recoveryCallback  : 3번째해는 해당 구간이 실행된다.
            return item.unknownName();
        });
    }
}

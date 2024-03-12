package com.example.springbatchexample.part3;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

import java.net.SocketTimeoutException;

public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person> {

    private final RetryTemplate retryTemplate;

    public PersonValidationRetryProcessor() {
        this.retryTemplate = new RetryTemplateBuilder()
                .maxAttempts(2)
                .retryOn(NotFoundNameException.class) // 재시도 되는 조건 이 에러가 아니면 maxAttempt 만큼 실행하지 않고 바로 recoveryCallback 실행된다.
                .build();
    }

    @Override
    public Person process(Person item) throws Exception {
        return this.retryTemplate.execute(context -> {
            //retryCallBack : 이 구간이 2번 재시도 된다
            if(item.isNotEmptyName()) return item;

            throw new NotFoundNameException();
        }, context -> {
            // recoveryCallback  : 재시도가 불가한 경우에 실행된다.
            return item.unknownName();
        });
    }
}

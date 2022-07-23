package com.example.springbatchexample.part3;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;


public class CustomItemReader<T> implements ItemReader<T> {
    // java Collection list를 reader 로 처리하는 CustomItemReader 구현하기

    private final List<T> items;

    public CustomItemReader(List<T> items) {
        this.items = new ArrayList<>(items);
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(!items.isEmpty()) {
            return items.remove(0); // return 과 동시에 제거
        }
        return null; // null 를 리턴해야 chunk 반복의 끝임을 의미한다.
    }

}

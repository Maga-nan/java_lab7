package com.example.lab.Service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RequestCounterService {

    private final AtomicLong counter = new AtomicLong(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public long getCount() {
        return counter.get();
    }

    public void reset() {
        counter.set(0);
    }
}

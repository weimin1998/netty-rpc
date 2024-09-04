package com.weimin.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceIdGenerator {

    private static final AtomicInteger ID = new AtomicInteger();

    public static int nextId(){
        return ID.getAndIncrement();
    }
}

package com.ringo.it.util;

public class IdGenerator {

    public static Long getNewId() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis();
    }
}

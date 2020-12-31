package com.example.rxjava2demo;

import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2017/7/27.
 */

public class Utils {
    public static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException ignored) {
            //intentionally ignored
        }
    }

    public static void log(Object msg) {
        System.out.println(
                Thread.currentThread().getName() +
                        ": " + msg);
    }

    public static void findCaller() {
        final Throwable mThrowable = new Throwable();
        final StackTraceElement[] elements = mThrowable.getStackTrace();
        final int len = elements.length;
        StackTraceElement item;
        for (int i = 1; i < len; i++) {
            item = elements[i];
            log("StackTrace: " +
                    item.getClassName() + "." + item.getMethodName()
                    + " ---" + item.getLineNumber() + " line");
        }
    }


}

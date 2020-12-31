package com.example.rxjava2demo;

/*
    参考：Rxjava2总结 -- https://luhaoaimama1.github.io/2017/07/31/rxjava/
 */



import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class CreatingObservables {
    @Test
    public void create_001() {
        // create:创建一个具有发射能力的Observable
        Observable.create(e -> {
            e.onNext("Love");
            e.onNext("For");
            e.onNext("You!");
            e.onComplete();
        }).subscribe(Utils::log);
    }

    @Test
    public void just_001() {
        // just:只是简单的原样发射,可将数组或Iterable当做单个数据。它接受一至九个参数
        Observable.just("Love", "For", "You!")
                .subscribe(Utils::log);
    }

    @Test
    public void empty_001() {
        // empty:创建一个不发射任何数据但是正常终止的Observable
        Observable.empty()
                .subscribe(Utils::log, Utils::log, () -> Utils.log("onComplete"));
    }

    @Test
    public void never_001() {
        // never:创建一个不发射数据也不终止的Observable
        Observable.never()
                .takeUntil(Observable.timer(5, TimeUnit.SECONDS))
                .subscribe(Utils::log, Utils::log, () -> Utils.log("onComplete"));

        Utils.sleep(8, TimeUnit.SECONDS);
    }

    @Test
    public void error_001() {
        // error:创建一个不发射数据以一个错误终止的Observable
        Observable.error(new Throwable("O__O"))
                .subscribe(Utils::log, Utils::log, () -> Utils.log("onComplete"));
    }

    @Test
    public void timer_001() {
        // timer 在延迟一段给定的时间后发射一个简单的数字0L
        Observable.timer(1000, TimeUnit.MILLISECONDS, Schedulers.trampoline())
                .subscribe(Utils::log);

        Utils.log("timer_001 end");
    }

    @Test
    public void range_001() {
        /*
        从start开始以步长1递增发送count个数据
        range:
            start:起始值
            count:一个是范围的数据的数目。0不发送，负数异常
         */
        Observable.range(5, 3)
                //输出 5,6,7
                .subscribe(Utils::log);
    }

    @Test
    public void intervalRange_001() {
        TestObserverEx<Long> observer = new TestObserverEx<>();
        /*
        intervalRange:
            start,count:同range
            initialDelay 发送第一个值的延迟时间
            period 每两个发射物的间隔时间
            unit,scheduler 额你懂的
         */
        Observable.intervalRange(5, 20, 3000, 100,
                                 TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe(observer);

        observer.awaitTerminalEvent();
    }

    @Test
    public void interval_001() {
        TestObserverEx<Long> observer = new TestObserverEx<>();
        // period 这个值一旦设定后是不可变化的
        Observable.interval(3000, 100, TimeUnit.MILLISECONDS, Schedulers.io())
                .take(20)
                .subscribe(observer);
        observer.awaitTerminalEvent();
    }

    @Test
    public void defer_001() {
        TestObserverEx<String> observer = new TestObserverEx<>();
        // defer 直到有观察者订阅时才创建Observable，并且为每个观察者创建一个新的Observable
        Observable.defer(() -> Observable.just("Love", "For", "You!"))
                .subscribe(observer);
        observer.awaitTerminalEvent();
    }

    @Test
    public void fromArray_001() {
        Integer[] items = {0, 1, 2, 3, 4, 5};
        Observable.fromArray(items).subscribe(Utils::log);
    }

    @Test
    public void fromCallable_001() {
        Observable.fromCallable(() -> Arrays.asList("hello", "gaga"))
                .subscribe(Utils::log);
    }

    @Test
    public void fromIterable_001() {
        Observable.fromIterable(Arrays.asList("one", "two", "three"))
                .subscribe(Utils::log);
    }

    @Test
    public void fromFuture_001() {
        Observable.fromFuture(Observable.just(1).toFuture())
                .doOnComplete(() -> System.out.println("complete"))
                .subscribe(Utils::log);
    }

}

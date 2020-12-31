package com.example.rxjava2demo;

/*
    参考：Rxjava2总结 -- https://luhaoaimama1.github.io/2017/07/31/rxjava/
 */

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;

public class ConnectableObservableOperators {
    /*
    ConnectableObservable：可连接的Observable在 被订阅时并不开始发射数据，只有在它的 connect()
    被调用时才开始用这种方法，你可以 等所有的潜在订阅者都订阅了这个Observable之后才开始发射数据。
    即使没有任何订阅者订阅它，你也可以使用 connect 让他发射
     */

    @Test
    public void replay_001() {
        /*
        replay(Observable的方法): 每次订阅 都对单个订阅的重复播放一边.
        bufferSize:对源发射队列的缓存数量, 从而对新订阅的进行发射；
        Observable的方法 返回是ConnectableObservable
        切记要让ConnectableObservable具有重播的能力,必须Observable的时候调用replay,而不是
        ConnectableObservable 的时候调用replay
         */
        ConnectableObservable<Integer> co = Observable.just(1, 2, 3)
                //类似 publish直接转成 ConnectableObservable  切记要重复播放的话必须Obserable的时候调用replay
                //而不是ConnectableObservable 的时候调用replay 所以 .publish().replay()则无效
                .replay(3);//重复播放的 是1  2  3
        //           .replay(2);//重复播放的 是 2  3
        co.doOnSubscribe(disposable -> System.out.print("订阅1："))
                .doFinally(() -> System.out.println("订阅1 Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
        co.connect();//此时开始发射数据 不同与 refCount 只发送一次

        co.doOnSubscribe(disposable -> System.out.print("订阅2："))
                .doFinally(() -> System.out.println("订阅2 Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
        co.doOnSubscribe(disposable -> System.out.print("订阅3："))
                .doFinally(() -> System.out.println("订阅3 Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
    }

    @Test
    public void replay_002() {
        ConnectableObservable<Integer> co = Observable.just(1, 2, 3)
                //类似 publish直接转成 ConnectableObservable  切记要重复播放的话必须Obserable的时候调用replay
                //而不是ConnectableObservable 的时候调用replay 所以 .publish().replay()则无效
                .replay(2);//重复播放的 是 2  3

        co.doOnSubscribe(disposable -> System.out.print("订阅1："))
                .doFinally(() -> System.out.println("订阅1 Final"))
                .subscribe(integer -> System.out.print(integer + ","));
        co.connect();//此时开始发射数据 不同与 refCount 只发送一次

        co.doOnSubscribe(disposable -> System.out.print("订阅2："))
                .doFinally(() -> System.out.println("订阅2 Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
        co.doOnSubscribe(disposable -> System.out.print("订阅3："))
                .doFinally(() -> System.out.println("订阅3 Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
    }

    @Test
    public void publish_001() {
        // publish(Observable的方法):将普通的Observable转换为ConnectableObservable
        ConnectableObservable<Integer> co = Observable.just(1, 2, 3).publish();
        co.subscribe(integer -> System.out.println("订阅1：" + integer));
        co.subscribe(integer -> System.out.println("订阅2：" + integer));
        co.subscribe(integer -> System.out.println("订阅3：" + integer));
        co.connect();//此时开始发射数据

        co.subscribe(integer -> System.out.println("订阅4：" + integer));
    }

    @Test
    public void refCount_001() {
        /*
        RefCount操作符把从一个可连接的Observable连接和断开的过程自动化了。它操作一个可连接的Observable，
        返回一个普通的Observable。当第一个订阅者订阅这个Observable时，RefCount连接到下层的可连接Observable。
        RefCount跟踪有多少个观察者订阅它，直到最后一个观察者完成才断开与下层可连接Observable的连接。
         */
        Observable<Integer> co = Observable.just(1, 2, 3)
                .publish()
                //类似于reply  跟时间线有关  订阅开始就开始发送
                .refCount();

        Disposable disposable1 = co.doOnSubscribe(disposable -> System.out.print("订阅1："))
                .doFinally(() -> System.out.println("订阅1：Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
        Utils.log("----");
        co.doOnSubscribe(disposable -> System.out.print("订阅2："))
                .doFinally(() -> System.out.println("订阅2：Final"))
                .subscribe(integer -> System.out.print(integer + "\t"));
        Utils.log("----");
        Observable.timer(300, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> {
                    co.doOnSubscribe(disposable -> System.out.print("订阅3："))
                            .doFinally(() -> System.out.println("订阅3：Final"))
                            .subscribe(integer -> System.out.print(integer + "\t"));
                }).blockingSubscribe();

        disposable1.dispose();
    }

    @Test
    public void refCount_002() {
        Observable<Long> interval = Observable.interval(100L,
                                                        TimeUnit.MILLISECONDS)
                                                .take(10);
        Observable<Long> refCount = interval.publish().refCount();

        Disposable disposable1 = refCount.subscribe(val -> Utils.log("First: " + val));
        Disposable disposable2 = refCount.subscribe(val -> Utils.log("Second: " + val));
        Utils.sleep(300, TimeUnit.MILLISECONDS);
        disposable1.dispose();

        Disposable disposable3 = refCount.subscribe(val -> Utils.log("* Third: " + val)); //从3开始
        Utils.sleep(300, TimeUnit.MILLISECONDS);
        disposable3.dispose();

        Utils.sleep(500, TimeUnit.MILLISECONDS);
        disposable2.dispose();
    }

    @Test
    public void refCount_003() {
        Observable<Long> interval = Observable.interval(100L,
                                                        TimeUnit.MILLISECONDS)
                .take(10);
        Observable<Long> refCount = interval.publish().refCount();

        Disposable disposable1 = refCount.subscribe(val -> Utils.log("First: " + val));
        Disposable disposable2 = refCount.subscribe(val -> Utils.log("Second: " + val));
        Utils.sleep(300, TimeUnit.MILLISECONDS);
        disposable1.dispose();
        disposable2.dispose();

        //  it will begin emitting the sequence from the beginning
        Disposable disposable3 = refCount.subscribe(val -> Utils.log("* Third: " + val)); //从0开始
        Utils.sleep(300, TimeUnit.MILLISECONDS);
        disposable3.dispose();

        Utils.sleep(500, TimeUnit.MILLISECONDS);

    }

}

package com.example.rxjava2demo;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;

/**
 * Created by admin on 2017/12/18.
 */

public class TestObserverEx<T> extends TestObserver<T> {
    @Override
    public void onSubscribe(Disposable s) {
        super.onSubscribe(s);
        Utils.log("onSubscribe s=" + s);
    }

    @Override
    public void onNext(T t) {
        super.onNext(t);
        Utils.log("onNext t=" + t);
    }

    @Override
    public void onError(Throwable t) {
        super.onError(t);
        Utils.log("onError t=" + t);
    }

    @Override
    public void onComplete() {
        super.onComplete();
        Utils.log("onComplete");
    }

    @Override
    public void onSuccess(T value) {
        super.onSuccess(value);
        Utils.log("onSuccess value=" + value);
    }
}

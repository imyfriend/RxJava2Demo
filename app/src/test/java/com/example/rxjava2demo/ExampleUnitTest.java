package com.example.rxjava2demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.schedulers.IoScheduler;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Before
    public void setup() {
        RxJavaPlugins.reset();
        //设置Schedulers.io()返回的线程
        RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                //返回当前的工作线程，这样测试方法与之都是运行在同一个线程了，从而实现异步变同步。
                return Schedulers.trampoline();
            }
        });
    }

    @Test
    public void test001() {
        Observable.just(1)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe();
    }

    @Test
    public void test002() {
        class House {
            House(String communityName, String desc) {
                this.communityName = communityName;
                this.desc = desc;
            }
            String communityName;
            String desc;
        }
        List<House> houses = new ArrayList<>();
        houses.add(new House("中粮·海景壹号", "中粮海景壹号新出大平层！总价4500W起"));
        houses.add(new House("竹园新村", "满五唯一，黄金地段"));
        houses.add(new House("中粮·海景壹号", "毗邻汤臣一品"));
        houses.add(new House("竹园新村", "顶层户型，两室一厅"));
        houses.add(new House("中粮·海景壹号", "南北通透，豪华五房"));
        Observable<GroupedObservable<String, House>> groupByCommunityNameObservable = Observable.fromIterable(houses)
                .groupBy(new Function<House, String>() {

                    public String apply(House house) {
                        return house.communityName;
                    }
                });
       groupByCommunityNameObservable.concatMap(Functions.identity())
               .subscribe(house -> System.out.println("小区:"+house.communityName+"; 房源描述:"+house.desc));
        /*Observable.concat(groupByCommunityNameObservable)
                .subscribe(new Consumer<House>() {

                    public void accept(House house) {
                        System.out.println("小区:"+house.communityName+"; 房源描述:"+house.desc);
                    }
                });*/
    }

    @Test
    public void gson_001() {
        String string = "{\"name\":\"abc\",\n" + "\"age\":\"18\"}";
        Gson gson = new Gson();
        User user = gson.fromJson(string, new TypeToken<User>(){}.getType());
        System.out.println(user);
    }

    @Test
    public void gson_002() {
        String string = "{\"name\":\"abc\",\n" + "\"age\":\"18\"}";
        User user = gsonGeneric(string);
        System.out.println(user);
    }

    <T> T gsonGeneric(String jsonStr) {
        Type type = new TypeToken<T>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, type);
    }

    @Test
    public void gson_003() {
        Result<User> userResult = new Result<>();
        userResult.data = new User();
        userResult.data.name = "Toom";
        userResult.data.age = "11";
        Gson gson = new Gson();
        String string = gson.toJson(userResult);
        System.out.println("string=" + string);
        Result<User> result = fromJsonObject(string, User.class);
        System.out.println(result.data);
    }

    static <T> Result<T> fromJsonObject(String jsonStr, Class<T> clazz) {
        Type type = new ParameterizedTypeImpl(Result.class, new Class[]{clazz});
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, type);
    }

    static class Result<T> {
        T data;
    }

    static class ParameterizedTypeImpl implements ParameterizedType {
        private final Class raw;
        private final Type[] args;
        public ParameterizedTypeImpl(Class raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }
        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }
        @Override
        public Type getRawType() {
            return raw;
        }
        @Override
        public Type getOwnerType() {
            Optional<Type> possible = Optional.empty();
            return possible.get();
        }
    }

    @Test
    public void optional_001() {
        Optional<Type> possible = Optional.empty();
        System.out.println("possible=" + possible.orElse(null));
    }

    @Test
    public void publishSubject_001() {
        /*
        publishSubject(subject里最常用的):可以说是最正常的Subject，从那里订阅就从那里开始发送数据。
        如果原始的Observable因为发生了一个错误而终止，PublishSubject将不会发射任何数据，只 是简单的向前传递这个错误通知。
         */
        PublishSubject<Integer> bs = PublishSubject.create();
        bs.subscribe(o -> System.out.println("1:"+o));
        bs.onNext(1);
        bs.onNext(2);
        bs.subscribe(o -> System.out.println("2:"+o));
        bs.onNext(3);
        bs.onComplete();
        bs.subscribe(o -> System.out.println("3:"+o));
    }

    @Test
    public void test_010() {
        Observable<String> observable = Observable.unsafeCreate(new ObservableSource<String>(){
            PublishSubject<String> cacheSubject = PublishSubject.create();
            PublishSubject<String> dbSubject = PublishSubject.create();


            @Override
            public void subscribe(@NonNull Observer<? super String> observer) {
                cacheSubject.doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("observer onComplete");
                        //observer.onComplete();
                    }
                }).subscribe(observer);
                dbSubject.doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("cacheSubject onComplete");
                        //cacheSubject.onComplete();
                    }
                }).subscribe(cacheSubject);

                String data = null;
                if (data != null) {
                    cacheSubject.onNext(data);
                } else {
                    loadFromDb();
                }
                loadFromNet();
            }

            private void loadFromDb() {
                String data = "select * from table";
                dbSubject.onNext(data);
            }

            private void loadFromNet() {
                Observable<String> netObservable = Observable.unsafeCreate(new ObservableSource<String>() {
                    @Override
                    public void subscribe(@NonNull Observer<? super String> observer) {
                        String data = "get data from net";
                        observer.onNext(data);
                        observer.onComplete();
                    }
                });
                netObservable.doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("dbSubject onComplete");
                        //dbSubject.onComplete();
                    }
                }).subscribe(dbSubject);
            }
        });

        observable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                System.out.println("onSubscribe");
            }

            @Override
            public void onNext(@NonNull String s) {
                System.out.println("onNext: " + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        });
    }
}
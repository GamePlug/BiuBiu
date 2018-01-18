package com.leichao.retrofit;

import rx.Subscriber;

/**
 *
 * Created by leichao on 2017/7/22.
 */

public abstract class SupportBackpressureSubscriber<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {}

    @Override
    public void onError(Throwable e) {}

    @Override
    public void onStart() {
        super.onStart();
        request(1);
    }

    @Override
    public void onNext(T t) {
        request(1);
    }
}

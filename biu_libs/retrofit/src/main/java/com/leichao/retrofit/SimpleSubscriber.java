package com.leichao.retrofit;

import android.content.Context;

import rx.Subscriber;

/**
 * 简化Subscriber
 * Created by leichao on 2017/4/12.
 */

public abstract class SimpleSubscriber<T> extends Subscriber<T> {

    private DialogLoading loading;

    public SimpleSubscriber() {
        super();
    }

    public SimpleSubscriber(Context context) {
        super();
        loading = new DialogLoading(context);
        loading.show();
    }

    // 因为此方法未使用到，所以暂时注释，用来解除方法未被使用的警告
    /*public SimpleSubscriber(Context context, String message) {
        super();
        loading = new DialogLoading(context);
        loading.show();
    }*/

    @Override
    public void onError(Throwable e) {
        if(loading!=null){
            loading.dismiss();
        }
        onFailure(e);
        onCompleted();
    }

    @Override
    public void onNext(T t) {
        RetrofitUtil.log("result：" + t.toString());
        if(loading!=null){
            loading.dismiss();
        }
        onSuccess(t);
    }

    @Override
    public void onCompleted() {}

    public abstract void onSuccess(T result);

    public void onFailure(Throwable e) {}

}

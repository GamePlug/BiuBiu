package com.leichao.retrofit;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.leichao.network.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Retrofit与RxJava的网络请求回调
 * onSuccess成功，onFailure失败，onCompleted完成
 */
public abstract class SubscriberCallBack<T> extends Subscriber<ApiResult<T>> {

    private Context context;
    private DialogLoading loading;
    private boolean isStopReconnect;

    public SubscriberCallBack() {
        super();
    }

    public SubscriberCallBack(Context context) {
        super();
        this.context = context;
        if (context != null) {
            loading = new DialogLoading(context);
            loading.show();
        }
    }

    public SubscriberCallBack(Context context, String message) {
        super();
        this.context = context;
        if (context != null) {
            loading = new DialogLoading(context, message);
            loading.show();
        }
    }

    /**
     * 网络请求回调构造方法
     * @param context Context上下文，不为null代表显示loading，为null不显示
     * @param message loading显示的信息
     * @param cancelable loading是否可以点击返回键取消，true可以，false不可以，默认为true
     */
    public SubscriberCallBack(Context context, String message, boolean cancelable) {
        super();
        this.context = context;
        if (context != null) {
            loading = new DialogLoading(context, message, cancelable);
            loading.show();
        }
    }

    // 用于在重写了isReconnect方法时，停止重连。
    public final void stopReconnect() {
        isStopReconnect = true;
    }

    @Override
    public void onError(Throwable e) {
        RetrofitUtil.log("result:" + e.toString());
        dismissLoading();
        ApiResult<T> apiResult = new ApiResult<>();
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            int code = httpException.code();
            apiResult.setCode(String.valueOf(httpException.code()));
            if (code == 504) {
                // 网络异常，且开启了缓存但获取缓存失败时。
                apiResult.setStatus(ApiResult.Status.ERROR_NET);
                apiResult.setMessage(RetrofitConfig.getInstance().getApplication().getString(R.string.error_net));
            } else {
                // 服务器端程序抛出异常。
                apiResult.setStatus(ApiResult.Status.ERROR_OTHER);
                apiResult.setMessage(RetrofitConfig.getInstance().getApplication().getString(R.string.error_other));
                if (code == 500) {
                    uploadErrorToServer(httpException.response().errorBody());
                }
            }
        } else if (e instanceof IOException) {// ConnectException,SocketTimeoutException,UnknownHostException
            // 网络异常
            apiResult.setStatus(ApiResult.Status.ERROR_NET);
            apiResult.setMessage(RetrofitConfig.getInstance().getApplication().getString(R.string.error_net));
        } else {
            // 客户端程序异常，一般是开发者在回调方法中发生了异常
            e.printStackTrace();
            apiResult.setStatus(ApiResult.Status.ERROR_OTHER);
            apiResult.setMessage(RetrofitConfig.getInstance().getApplication().getString(R.string.error_other));
        }
        // 方便失败后需要重新请求的场景，如需停止重连需要isReconnect返回false或调用stopReconnect方法。
        if (isReconnect(apiResult)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isStopReconnect) {
                        onReconnect();
                    }
                }
            }, 3000);
        } else {
            failure(apiResult);
            completed();
        }
    }

    @Override
    public void onNext(ApiResult<T> t) {
        dismissLoading();
        if (ApiResult.Status.ERROR_JSON == t.getStatus()) {
            failure(t);
        } else if ("success".equals(t.getCode())) {
            t.setStatus(ApiResult.Status.TYPE_TRUE);
            success(t);
        } else {
            t.setStatus(ApiResult.Status.TYPE_FALSE);
            // 统一处理登录时效，登录冲突等情况
            if ("-2".equals(t.getCode())) {
                RetrofitConfig.getInstance().conflictLogin();
            } else {
                failure(t);
            }
        }
    }

    /**
     * 网络请求成功的回调方法，必须重写
     */
    public abstract void onSuccess(ApiResult<T> apiResult);

    /**
     * 网络请求失败的回调方法，选择重写
     */
    public void onFailure(ApiResult<T> apiResult) {
        if (apiResult.getStatus() == ApiResult.Status.ERROR_JSON) {
            return;// 屏蔽解析错误Toast
        } else if ("1002".equals(apiResult.getCode())) {
            return;// 屏蔽参数错误Toast
        }
        /*final String message = apiResult.getMessage();
        if (!TextUtils.isEmpty(message)) {
            RetrofitConfig.getInstance().toast(message);
        }*/
        RetrofitConfig.getInstance().toast(apiResult);
    }

    /**
     * 网络请求完成的回调方法，选择重写
     */
    @Override
    public void onCompleted() {

    }

    /**
     * 网络请求重连的回调方法，选择重写，慎用
     */
    public boolean isReconnect(ApiResult<T> apiResult) {
        return false;
    }

    /**
     * 网络请求重连的回调方法，选择重写，慎用
     */
    public void onReconnect() {

    }

    private void success(ApiResult<T> apiResult) {
        try {
            onSuccess(apiResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void failure(ApiResult<T> apiResult) {
        try {
            onFailure(apiResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completed() {
        try {
            onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissLoading() {
        if(loading == null || context == null) {
            return;
        }
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        loading.dismiss();
    }

    /**
     * 将服务器异常信息上传到服务器
     */
    private void uploadErrorToServer(ResponseBody errorBody) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", RetrofitConfig.getInstance().getUserId());
            params.put("errorInfo", errorBody.string());
            params.put("type", "userId".equals(RetrofitConfig.getInstance().getUserIdKey()) ? 1 : 2);
            params.put("errorType", 2);
            AppClient.retrofit().create(IStores.class).upErrorInfo(params)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleSubscriber<ApiResult<Object>>() {
                        @Override
                        public void onSuccess(ApiResult<Object> result) {
                            RetrofitUtil.log("upErrorInfo onSuccess");
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            RetrofitUtil.log("upErrorInfo Failure:"+e.getMessage());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

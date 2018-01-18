package com.leichao.retrofit;

import android.app.Application;

/**
 * Retrofit配置类
 * Created by leichao on 2017/4/15.
 */

public class RetrofitConfig {

    private static RetrofitConfig instance;
    private CallBack callBack;

    private RetrofitConfig() {

    }

    public static RetrofitConfig getInstance() {
        if (instance == null) {
            instance = new RetrofitConfig();
        }
        return instance;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean isDebug() {
        return callBack.isDebug();
    }

    public Application getApplication() {
        return callBack.getApplication();
    }

    public String getBaseUrl() {
        return callBack.getBaseUrl();
    }

    public String getUserId() {
        return callBack.getUserId();
    }

    public String getUserIdKey() {
        return callBack.getUserIdKey();
    }

    public String getSecret() {
        return callBack.getSecret();
    }

    public String getSecretKey() {
        return callBack.getSecretKey();
    }

    public String getLanguage() {
        return callBack.getLanguage();
    }

    public String getVersion() {
        return callBack.getVersion();
    }

    public void conflictLogin() {
        callBack.conflictLogin();
    }

    public void loginOut() {
        callBack.loginOut();
    }

    public <T> void toast(ApiResult<T> apiResult) {
        callBack.toast(apiResult);
    }

    public interface CallBack {
        boolean isDebug();
        Application getApplication();
        String getBaseUrl();
        String getUserId();
        String getUserIdKey();
        String getSecret();
        String getSecretKey();
        String getLanguage();
        String getVersion();
        void conflictLogin();
        void loginOut();
        <T> void toast(ApiResult<T> apiResult);
    }
}

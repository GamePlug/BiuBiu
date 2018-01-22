package com.leichao.push;

import android.content.Context;

/**
 * Mqtt推送配置类
 * Created by leichao on 2017/4/15.
 */

public class PushConfig {

    private static PushConfig instance;
    private CallBack callBack;

    private PushConfig() {

    }

    public static PushConfig getInstance() {
        if (instance == null) {
            instance = new PushConfig();
        }
        return instance;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean isDebug() {
        return callBack.isDebug();
    }

    public String getUserId() {
        return callBack.getUserId();
    }

    public String getServerUri() {
        return callBack.getServerUri();
    }

    public String getClientId() {
        return callBack.getClientId();
    }

    public String[] getTopic() {
        return callBack.getTopic();
    }

    public void conflictLogin() {
        callBack.conflictLogin();
    }

    public void notifyOpened(Context context, PushMessage message) {
        callBack.notifyOpened(context, message);
    }

    public interface CallBack {
        boolean isDebug();
        String getUserId();
        String getServerUri();
        String getClientId();
        String[] getTopic();
        void conflictLogin();
        void notifyOpened(Context context, PushMessage message);
    }
}

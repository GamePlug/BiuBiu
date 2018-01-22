package com.leichao.push;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息监听管理类
 * Created by leichao on 2017/3/11.
 */
public class PushManager {

    public static final String PUSH = "push";
    private static volatile PushManager instance;

    private List<PushListener> listeners;

    private PushManager() {
        listeners = new ArrayList<>();
    }

    public static PushManager getInstance() {
        if (instance == null) {
            synchronized (PushManager.class) {
                if (instance == null) {
                    instance = new PushManager();
                }
            }
        }
        return instance;
    }

    public void addMessage(final PushMessage message) {
        try {
            if (message == null) {
                return;
            }
            Log.e("MULA", "message:" + message.toString());
            //PushNotification.getInstance().show(message);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (PushListener listener : listeners) {
                        listener.receiveMessage(message);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPushListener(PushListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePushListener(PushListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

}

package com.leichao.mqtt;

import android.util.Log;

/**
 * IM工具类
 * Created by leichao on 2017/7/7.
 */

public class MqttUtil {

    public static final String TAG = "MQTT";// 日志打印TAG

    static boolean DEBUG = true;

    public static void log(String log) {
        if (DEBUG) {
            Log.i(TAG, log);
        }
    }

}

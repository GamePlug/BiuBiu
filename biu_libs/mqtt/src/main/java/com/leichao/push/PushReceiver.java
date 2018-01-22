package com.leichao.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 极光推送接收
 * Created by leichao on 2017/4/16.
 */

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if ("com.mula.mqtt.intent.PUSH_INIT".equals(intent.getAction())) {
                // 初始化MQTT推送
                PushUtil.init(context);

            } else if ("com.mula.mqtt.intent.NOTIFICATION_OPENED".equals(intent.getAction())) {
                // 通知栏点击事件回调
                if (context.getPackageName().equals(intent.getStringExtra("CLIENT"))) {
                    PushMessage message = (PushMessage) intent.getSerializableExtra("PUSH_MESSAGE");
                    PushConfig.getInstance().notifyOpened(context, message);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

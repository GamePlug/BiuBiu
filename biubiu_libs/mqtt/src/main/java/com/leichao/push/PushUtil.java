package com.leichao.push;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.leichao.mqtt.MqttSimpleListener;
import com.leichao.mqtt.MulaMqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 推送工具类
 * Created by leichao on 2017/9/8.
 */

public class PushUtil {

    private static boolean isInit;

    /**
     * 初始化，在Application中调用
     */
    public static void initSync(Context context) {
        context.sendBroadcast(new Intent("com.mula.mqtt.intent.PUSH_INIT"));
    }

    /**
     * 初始化，在非Application中调用
     */
    public static void init(final Context context) {
        if (isInit) {
            return;
        }
        isInit = true;
        String serverUri = PushConfig.getInstance().getServerUri();
        String clientId = PushConfig.getInstance().getClientId();
        String[] topic = PushConfig.getInstance().getTopic();
        MulaMqtt.init(context, serverUri, clientId, topic);
        MulaMqtt.connect();
        MulaMqtt.setGlobalListener(new MqttSimpleListener() {
            @Override
            public void onReceiveMessage(MqttMessage message) {
                // 判断是否登录
                if (TextUtils.isEmpty(PushConfig.getInstance().getUserId())) {
                    return;
                }
                // 获取消息内容
                String messageStr = new String(message.getPayload());
                // 解析并下发消息
                PushMessage pushMessage = new PushMessage(messageStr);
                PushManager.getInstance().addMessage(pushMessage);
                // 显示消息通知栏
                if (isShowNotification(pushMessage)) {
                    Intent intent = new Intent("com.mula.mqtt.intent.NOTIFICATION_OPENED");
                    intent.putExtra("CLIENT", context.getPackageName());
                    intent.putExtra("PUSH_MESSAGE", pushMessage);
                    MulaMqtt.showNotification(context, intent, "", pushMessage.getTitle());
                }
                // 异地登录提示
                if (pushMessage.parseType() == PushMessage.TYPE.TYPE_221
                        && pushMessage.getResult().equals(PushConfig.getInstance().getUserId())) {
                    PushConfig.getInstance().conflictLogin();
                }
            }
        });
    }

    /**
     * 检查MQTT是否连接，若未连接则重连
     */
    public static void checkForReconnect() {
        if (!MulaMqtt.isConnected()) {
            MulaMqtt.connect();
        }
    }

    /**
     * 订阅指定主题
     */
    public static void subscribe(String topic) {
        if (!MulaMqtt.isSubscribe(topic)) {
            MulaMqtt.subscribe(new String[]{topic});
        }
    }

    /**
     * 是否显示通知栏
     */
    private static boolean isShowNotification(PushMessage message) {
        // 应用在后台
        /*if (AppStatus.isBackground) {
            //return true;
        }*/
        // 消息通知，优惠券消息，提现通知消息, 告知用户拉货完成
        return message.parseType() == PushMessage.TYPE.TYPE_222
                || message.parseType() == PushMessage.TYPE.TYPE_223
                || message.parseType() == PushMessage.TYPE.TYPE_224
                || message.parseType() == PushMessage.TYPE.TYPE_242
                || message.parseType() == PushMessage.TYPE.TYPE_2
                || message.parseType() == PushMessage.TYPE.TYPE_3
                || message.parseType() == PushMessage.TYPE.TYPE_CHAT;
    }

}

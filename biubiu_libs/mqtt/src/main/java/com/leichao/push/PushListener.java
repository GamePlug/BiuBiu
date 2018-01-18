package com.leichao.push;

/**
 * 推送消息监听器
 * Created by leichao on 2017/3/11.
 */

public interface PushListener {

    void receiveMessage(PushMessage message);

}

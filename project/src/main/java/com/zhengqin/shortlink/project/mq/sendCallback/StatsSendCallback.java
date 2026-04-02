package com.zhengqin.shortlink.project.mq.sendCallback;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

@Slf4j
public class StatsSendCallback implements SendCallback {
    @Override
    public void onSuccess(SendResult sendResult) {
        log.info("[消息访问统计监控] 消息发送结果：{}，消息ID：{}", sendResult.getSendStatus(), sendResult.getMsgId());
    }

    @Override
    public void onException(Throwable throwable) {

    }
}

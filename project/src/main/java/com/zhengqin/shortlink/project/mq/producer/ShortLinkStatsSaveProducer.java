package com.zhengqin.shortlink.project.mq.producer;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 短链接监控状态保存消息队列生产者
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveProducer {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String statsSaveTopic;

    /**
     * 发送延迟消费短链接统计
     */
    public void send(Map<String, Object> producerMap) {
        //雪花算法生成全局唯一的key
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        long keyId = snowflake.nextId();
        String key = String.valueOf(keyId);
        producerMap.put("key", key);
        String producerMapJSONStr = JSON.toJSONString(producerMap);
        Message<String> message = MessageBuilder
                .withPayload(producerMapJSONStr)
                .setHeader(MessageConst.PROPERTY_KEYS, key)
                .build();
        rocketMQTemplate.asyncSend(statsSaveTopic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("[消息访问统计监控] 消息发送结果：{}，消息ID：{}", sendResult.getSendStatus(), sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("消息发送失败,重新发送");
                rocketMQTemplate.asyncSend(statsSaveTopic, message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("[消息访问统计监控] 消息发送结果：{}，消息ID：{}", sendResult.getSendStatus(), sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("消息发送失败,消息体: {}",message);
                    }
                },2000L);
            }
        }, 2000L);
    }
}

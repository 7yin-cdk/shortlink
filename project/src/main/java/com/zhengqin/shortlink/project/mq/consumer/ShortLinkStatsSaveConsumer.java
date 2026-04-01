package com.zhengqin.shortlink.project.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.zhengqin.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.zhengqin.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.zhengqin.shortlink.project.dao.entity.LinkLocalStatsDO;
import com.zhengqin.shortlink.project.dao.entity.LinkOsStatsDO;
import com.zhengqin.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.zhengqin.shortlink.project.dao.mapper.LinkBrowserStatsMapper;
import com.zhengqin.shortlink.project.dao.mapper.LinkLocalStatsMapper;
import com.zhengqin.shortlink.project.dao.mapper.LinkOsStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zhengqin.shortlink.project.common.constant.RedisKeyConstant.CONSUMER_KEY;
import static com.zhengqin.shortlink.project.common.constant.RedisKeyConstant.LOCK_CONSUMER_KEY;

/**
 * 短链接监控状态保存消息队列消费者
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${rocketmq.producer.topic}",
        consumerGroup = "${rocketmq.consumer.group}"
)
public class ShortLinkStatsSaveConsumer implements RocketMQListener<String> {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;
    private final LinkOsStatsMapper  linkOsStatsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String messageJSONStr) {
        Map<String,Object> stringObjectMap = JSON.parseObject(messageJSONStr, Map.class);
        //1.判断消息的key是否已经消费
        //2.已消费则提示并返回
        String key = (String)(stringObjectMap.get("key"));
        if(stringRedisTemplate.hasKey(key)){
            log.info("消息已被消费");
            return;
        }
        //3.未消费，获取这个key对应的分布式锁则获取每个数据库监控表的实体类对象
        RLock lock = redissonClient.getLock(LOCK_CONSUMER_KEY + key);
        lock.lock();
        if(stringRedisTemplate.hasKey(key)){
            log.info("消息已被消费");
            return;
        }
        // 4. 将 Map 中的 JSONObject 显式转为实体类
        LinkAccessStatsDO linkAccessStatsDO = JSON.parseObject(
                JSON.toJSONString(stringObjectMap.get("linkAccessStatsDO")),
                LinkAccessStatsDO.class
        );
        LinkLocalStatsDO linkLocalStatsDO = JSON.parseObject(
                JSON.toJSONString(stringObjectMap.get("linkLocalStatsDO")),
                LinkLocalStatsDO.class
        );
        LinkOsStatsDO linkOsStatsDO = JSON.parseObject(
                JSON.toJSONString(stringObjectMap.get("linkOsStatsDO")),
                LinkOsStatsDO.class
        );
        LinkBrowserStatsDO browserStatsDO = JSON.parseObject(
                JSON.toJSONString(stringObjectMap.get("browserStatsDO")),
                LinkBrowserStatsDO.class
        );
        //5.实体类对象不为空则插入到对应监控表
        if(linkAccessStatsDO != null){
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
        }
        if (linkLocalStatsDO != null){
            linkLocalStatsMapper.shortLinkLocalStats(linkLocalStatsDO);
        }
        if (linkOsStatsDO != null){
            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
        }
        if (browserStatsDO != null){
            linkBrowserStatsMapper.shortLinkBrowserState(browserStatsDO);
        }
        stringRedisTemplate.opsForValue().set(CONSUMER_KEY+key,key,7, TimeUnit.DAYS);
    }
}





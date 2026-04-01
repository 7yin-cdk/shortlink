package com.zhengqin.shortlink.project.common.constant;

/**
 * Redis Key 常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接映射关系key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link_goto_";

    /**
     * 短链接跳转分布式锁key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link_lock_goto_";

    /**
     * 短链接消费者消费特定消息分布式锁key
     */
    public static final String LOCK_CONSUMER_KEY = "short_link_lock_consumer_";

    /**
     * 短链接消费者消费特定消息key
     */
    public static final String CONSUMER_KEY = "short_link_consumer_";
}

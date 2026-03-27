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
}

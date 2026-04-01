package com.zhengqin.shortlink.project.common.constant;

/**
 * 短链接常量类
 */
public class ShortLinkConstant {

    /**
     * 永久短链接默认保存有效时间
     */
    public static final long DEFAULT_CACHE_EXPIRE_TIME = 1000*60*60*24*30L;

    /**
     * 高德地图获取地区接口地址
     */
    public static final String AMAP_REMOTE_URL = "https://restapi.amap.com/v3/ip";
}

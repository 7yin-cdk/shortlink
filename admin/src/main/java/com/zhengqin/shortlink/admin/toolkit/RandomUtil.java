package com.zhengqin.shortlink.admin.toolkit;

import java.util.Random;

/**
 * 分组id随机生成器
 */
public final class RandomUtil {

    // 字符集，包含数字和大小写字母
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成一个包含数字和英文字符的六位随机数
     *
     * @return 随机生成的六位字符串
     */
    public static String generateRandomString() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(6); // 6位长度的随机字符串

        // 生成六位随机字符
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARSET.length()); // 随机选取字符集中的一个字符
            stringBuilder.append(CHARSET.charAt(randomIndex)); // 将字符加入到结果中
        }

        return stringBuilder.toString();
    }
}
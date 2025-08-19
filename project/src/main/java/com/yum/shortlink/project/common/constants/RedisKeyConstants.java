package com.yum.shortlink.project.common.constants;

/**
 * Redis key常量类
 */
public class RedisKeyConstants {

    /**
     * 短链接跳转前缀 key
     */
    public static final String GOTO_SHORT_LINK_KEY = "shortLink_goto_%s";

    /**
     * 短链接跳转锁前缀 key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "shortLink_lock_goto_url_%s";

    /**
     * 短链接跳转前缀 key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "shortLink_lock_goto_%s";
}

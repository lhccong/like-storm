package com.cong.like.utils;


import com.cong.like.constant.ThumbConstant;


/**
 * Redis Key Util
 *
 * @author cong
 * @date 2025/04/21
 */
public class RedisKeyUtil {

    /**
     * 获取用户 Thumb 键
     *
     * @param userId 用户 ID
     * @return {@link String }
     */
    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
    }

    /**
     * 获取 临时点赞记录 key
     */
    public static String getTempThumbKey(String time) {
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }

}

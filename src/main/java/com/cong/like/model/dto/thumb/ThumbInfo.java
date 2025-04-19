package com.cong.like.model.dto.thumb;

import lombok.Data;
import java.io.Serializable;

@Data
public class ThumbInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 点赞ID
     */
    private Long thumbId;
    
    /**
     * 过期时间（时间戳，毫秒）
     */
    private Long expireTime;
} 
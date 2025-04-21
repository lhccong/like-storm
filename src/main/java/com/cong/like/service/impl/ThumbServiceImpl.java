package com.cong.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.like.common.ErrorCode;
import com.cong.like.constant.ThumbConstant;
import com.cong.like.excption.BusinessException;
import com.cong.like.mapper.ThumbMapper;
import com.cong.like.model.dto.thumb.DoThumbRequest;
import com.cong.like.model.dto.thumb.ThumbInfo;
import com.cong.like.model.entity.Blog;
import com.cong.like.model.entity.Thumb;
import com.cong.like.model.entity.User;
import com.cong.like.service.BlogService;
import com.cong.like.service.ThumbService;
import com.cong.like.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author cong
 */
@Service("thumbServiceDB")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Boolean exists = this.hasThumb(blogId, loginUser.getId());
                if (exists) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户已点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                boolean success = update && this.save(thumb);

                // 点赞记录存入 Redis
                if (success) {
                    String key = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString();
                    ThumbInfo thumbInfo = new ThumbInfo();
                    thumbInfo.setThumbId(thumb.getId());
                    // 设置30天后的时间戳
                    thumbInfo.setExpireTime(Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli());
                    redisTemplate.opsForHash().put(key, blogId.toString(), thumbInfo);
                }
                return success;
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                String key = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString();
                ThumbInfo thumbInfo = (ThumbInfo) redisTemplate.opsForHash().get(key, blogId.toString());
                if (thumbInfo == null || thumbInfo.getExpireTime() < System.currentTimeMillis()) {
                    //查询数据库
                    Thumb thumb = this.lambdaQuery()
                            .eq(Thumb::getUserId, loginUser.getId())
                            .eq(Thumb::getBlogId, blogId)
                            .one();
                    if (thumb == null) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户未点赞");
                    }
                    thumbInfo = new ThumbInfo();
                    thumbInfo.setThumbId(thumb.getId());
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                boolean success = update && this.removeById(thumbInfo.getThumbId());

                // 点赞记录从 Redis 删除
                if (success) {
                    redisTemplate.opsForHash().delete(key, blogId.toString());
                }
                return success;
            });
        }
    }


    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        String key = ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
        ThumbInfo thumbInfo = (ThumbInfo) redisTemplate.opsForHash().get(key, blogId.toString());
        if (thumbInfo == null) {
            Thumb thumb = this.lambdaQuery()
                    .eq(Thumb::getUserId, userId)
                    .eq(Thumb::getBlogId, blogId)
                    .one();
            if (thumb != null) {
                return true;
            }
            return false;
        }
        // 判断是否过期
        if (thumbInfo.getExpireTime() < System.currentTimeMillis()) {
            // 如果过期，删除该记录
            redisTemplate.opsForHash().delete(key, blogId.toString());
            return false;
        }
        return true;
    }

}





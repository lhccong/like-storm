package com.cong.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.like.common.ErrorCode;
import com.cong.like.constant.ThumbConstant;
import com.cong.like.excption.BusinessException;
import com.cong.like.mapper.ThumbMapper;
import com.cong.like.model.dto.thumb.DoThumbRequest;
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

import java.util.concurrent.TimeUnit;

/**
 * @author cong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

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
                    String field = blogId.toString();
                    redisTemplate.opsForHash().put(key, field, thumb.getId());
                    // 为每个field设置过期时间
                    String fieldKey = key + ":" + field;
                    redisTemplate.expire(fieldKey, 30, TimeUnit.DAYS);
                }
                // 更新成功才执行
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
                String field = blogId.toString();
                Long thumbId = ((Long) redisTemplate.opsForHash().get(key, field));
                if (thumbId == null) {
                    //查询数据库
                    Thumb thumb = this.lambdaQuery()
                           .eq(Thumb::getUserId, loginUser.getId())
                           .eq(Thumb::getBlogId, blogId)
                           .one();
                    if (thumb == null) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户未点赞");
                    }
                    thumbId = thumb.getId();
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                boolean success = update && this.removeById(thumbId);

                // 点赞记录从 Redis 删除
                if (success) {
                    redisTemplate.opsForHash().delete(key, field);
                    // 删除field的过期时间key
                    String fieldKey = key + ":" + field;
                    redisTemplate.delete(fieldKey);
                }
                return success;
            });
        }
    }


    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        String key = ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
        String field = blogId.toString();
        String fieldKey = key + ":" + field;
        // 检查field的过期时间key是否存在
        return Boolean.TRUE.equals(redisTemplate.hasKey(fieldKey)) && 
               redisTemplate.opsForHash().hasKey(key, field);
    }

}





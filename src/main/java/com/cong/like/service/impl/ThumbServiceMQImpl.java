package com.cong.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.like.common.ErrorCode;
import com.cong.like.constant.RedisLuaConstant;
import com.cong.like.excption.BusinessException;
import com.cong.like.listener.thumb.ThumbEvent;
import com.cong.like.mapper.ThumbMapper;
import com.cong.like.model.dto.thumb.DoThumbRequest;
import com.cong.like.model.entity.Thumb;
import com.cong.like.model.entity.User;
import com.cong.like.model.enums.LuaStatusEnum;
import com.cong.like.service.ThumbService;
import com.cong.like.service.UserService;
import com.cong.like.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 Pulsar 的点赞服务实现
 * @author cong
 */
@Service("thumbService")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceMQImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final PulsarTemplate<ThumbEvent> pulsarTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        checkThumbParams(doThumbRequest);
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogId();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本，点赞存入 Redis
        Long result = redisTemplate.execute(
                RedisLuaConstant.THUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );

        if (result == null || LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
        }

        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .type(ThumbEvent.EventType.INCR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("like-topic", thumbEvent).exceptionally(ex -> {
            // 回滚点赞
            redisTemplate.opsForHash().delete(userThumbKey, blogId.toString(), true);
            log.error("点赞已回滚，请人工检查异常，点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
            return null;
        });
        return true;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        checkThumbParams(doThumbRequest);
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogId();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);

        // 执行 Lua 脚本，点赞记录从 Redis 删除
        Long result = redisTemplate.execute(
                RedisLuaConstant.UNTHUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );

        if (result == null || LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
        }
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .type(ThumbEvent.EventType.DECR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("like-topic", thumbEvent).exceptionally(ex -> {
            // 回滚取消点赞
            redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), true);
            log.error("取消点赞已回滚，请人工检查异常，点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
            return null;
        });
        return true;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }

    private static void checkThumbParams(DoThumbRequest doThumbRequest) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "参数错误");
        }
    }
}
package com.cong.like.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.like.common.ErrorCode;
import com.cong.like.constant.RedisLuaConstant;
import com.cong.like.excption.BusinessException;
import com.cong.like.mapper.ThumbMapper;
import com.cong.like.model.dto.thumb.DoThumbRequest;
import com.cong.like.model.entity.Thumb;
import com.cong.like.model.entity.User;
import com.cong.like.model.enums.LuaStatusEnum;
import com.cong.like.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service("thumbService")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        checkThumbParams(doThumbRequest);
        //获取用户、帖子 id
        User loginUser = userService.getLoginUser(request);
        Long blogId = doThumbRequest.getBlogId();

        //获取当前整数秒（为了统一处理临时集合）
        String timeSlice = getTimeSlice();
        //生成 redis key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // 执行点赞 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        if (result == null || LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
        }

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    private static void checkThumbParams(DoThumbRequest doThumbRequest) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "参数错误");
        }
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        checkThumbParams(doThumbRequest);

        //获取用户、帖子 id
        User loginUser = userService.getLoginUser(request);
        Long blogId = doThumbRequest.getBlogId();

        //获取当前整数秒（为了统一处理临时集合）
        String timeSlice = getTimeSlice();
        //生成 redis key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
        // 执行取消 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );
        // 根据返回值处理结果
        if (result == null || result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户未点赞");
        }
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return null;
    }

    private String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }
}
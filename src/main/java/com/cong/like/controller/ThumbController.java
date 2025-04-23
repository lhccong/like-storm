package com.cong.like.controller;

import com.cong.like.common.BaseResponse;
import com.cong.like.common.ResultUtils;
import com.cong.like.model.dto.thumb.DoThumbRequest;
import com.cong.like.service.ThumbService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.Counter;

/**
 * @author cong
 */
@RestController
@RequestMapping("thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    private final Counter successCounter;
    private final Counter failureCounter;

    public ThumbController(MeterRegistry registry) {
        this.successCounter = Counter.builder("thumb.success.count")
                .description("Total successful thumb")
                .register(registry);
        this.failureCounter = Counter.builder("thumb.failure.count")
                .description("Total failed thumb")
                .register(registry);
    }
    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success;
        try {
            success = thumbService.doThumb(doThumbRequest, request);
            if (success) {
                //成功计数
                successCounter.increment();
            } else {
                //失败计数
                failureCounter.increment();
            }
        } catch (Exception e) {
            //失败计数
            failureCounter.increment();
            throw e;
        }
        return ResultUtils.success(success);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success = thumbService.undoThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }

}

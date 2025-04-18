package com.cong.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cong.like.model.dto.thumb.DoThumbRequest;
import com.cong.like.model.entity.Thumb;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author cong
 */
public interface ThumbService extends IService<Thumb> {

    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);
}

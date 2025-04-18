package com.cong.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cong.like.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author cong
 */
public interface UserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);
}

package com.cong.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.like.constant.UserConstant;
import com.cong.like.mapper.UserMapper;
import com.cong.like.model.entity.User;
import com.cong.like.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * @author cong
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }

}





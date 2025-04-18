package com.cong.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cong.like.model.entity.Blog;
import com.cong.like.model.entity.User;
import com.cong.like.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author cong
 */
public interface BlogService extends IService<Blog> {

    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    BlogVO getBlogVO(Blog blog, User loginUser);

    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);
}

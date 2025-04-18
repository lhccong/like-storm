package com.cong.like.controller;

import com.cong.like.common.BaseResponse;
import com.cong.like.common.ResultUtils;
import com.cong.like.model.entity.Blog;
import com.cong.like.model.vo.BlogVO;
import com.cong.like.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author cong
 */
@RestController
@RequestMapping("blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId, HttpServletRequest request) {
        BlogVO blogVO = blogService.getBlogVOById(blogId, request);
        return ResultUtils.success(blogVO);
    }

    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> list(HttpServletRequest request) {
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOList = blogService.getBlogVOList(blogList, request);
        return ResultUtils.success(blogVOList);
    }

}

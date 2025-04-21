package com.cong.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cong.like.model.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author cong
 */
public interface BlogMapper extends BaseMapper<Blog> {

    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}





package com.blog.service.impl;

import com.blog.entity.Blogs;
import com.blog.mapper.BlogsMapper;
import com.blog.service.IBlogsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Service
public class BlogsServiceImpl extends ServiceImpl<BlogsMapper, Blogs> implements IBlogsService {
    @Autowired
    private BlogsMapper blogsMapper;

    /*
     * @Author: kai.hu
     * @Date: 2025-5-5
     * @Description: 添加Blog
     */
    @Override
    public void addBlog(Blogs blogs) {
        blogsMapper.addBlog(blogs);
    }
}

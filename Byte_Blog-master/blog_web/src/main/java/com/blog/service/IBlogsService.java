package com.blog.service;

import com.blog.entity.Blogs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
public interface IBlogsService extends IService<Blogs> {

    /*
     * @Author: kai.hu
     * @Date: 2025-5-5
     * @Description: 添加Blog
     */
    void addBlog(Blogs blogs);
}

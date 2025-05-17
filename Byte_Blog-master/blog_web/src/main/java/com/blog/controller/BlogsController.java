package com.blog.controller;


import com.blog.entity.Blogs;
import com.blog.result.Result;
import com.blog.service.IBlogsService;
import com.blog.service.impl.BlogsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Slf4j
@RestController
@RequestMapping("/blogs")
public class BlogsController {
    @Autowired
    private IBlogsService blogsService;
    
    /*
     * @Author: kai.hu
     * @Date: 2025-5-5
     * @Description: 添加Blog
     */
    @PostMapping("/addBiog")
    public Result addBlog(@RequestBody Blogs blogs){
        log.info("添加Blog：{}" , blogs);
        blogsService.addBlog(blogs);
        return Result.success();

    }
}


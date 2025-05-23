package com.blog.service;

import com.blog.entity.Blogs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.BlogsDraft;
import com.blog.entity.BlogsPageQueryDTO;
import com.blog.entity.BlogsShowContext;
import com.blog.result.PageResult;
import com.blog.result.Result;

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
     * @Description: 创建Blog
     */
    Result<?> addBlog(Blogs blogs);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 获取Blog
     */
    Result<?> getBlog(Integer BlogsId);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 编辑Blog
     */
    Result<?> updateBlogs(Blogs blogs);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 管理员下架Blog
     */
    Result<?> removeBlog(Integer blogsId);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 分页查询，包含文章名字查询
     */
    PageResult pageQuery(BlogsPageQueryDTO blogsPageQueryDTO);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 获取需要渲染的信息
     */
    BlogsShowContext getBlogPDF(Integer blogsId);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-14
     * @Description: 实时保存博客内容
     */
    Result<?> saveBlogContent(BlogsDraft blogsDraft);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-15
     * @Description: 点击创建按钮，查询当前用户是否有未发布的草稿
     */
    Result<?> getDraft(Integer userId);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-14
     * @Description: 实时保存博客内容为草稿
     */
    Result<?> saveBlogDraft(Blogs blogs);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-19
     * @Description: 删除博客
     */
    Result<?> deleteBlog(Integer id);
}

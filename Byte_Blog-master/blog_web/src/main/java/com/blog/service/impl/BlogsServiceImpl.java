package com.blog.service.impl;

import cn.hutool.core.date.DateTime;
import com.blog.entity.*;
import com.blog.mapper.BlogsDraftMapper;
import com.blog.mapper.BlogsMapper;
import com.blog.mapper.UsersMapper;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.IBlogsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.utils.TextCheck;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private BlogsDraftMapper blogsDraftMapper;

    /*
     * @Author: kai.hu
     * @Date: 2025-5-5
     * @Description: 创建Blog
     */
    @Override
    public Result<?> addBlog(Blogs blogs) {
        // 判断标题是否为空
        if(blogs.getTitle() == null) {
            return Result.error("文章标题不能为空");
        }
        if(blogs.getContent() == null) {
            return Result.error("文章内容不能为空");
        }
        // 如果是要发布，需要对类容进行检测
        if(blogs.getStatus().equals("published")) {
            // 1,提取内容，去除标签
            String html = blogs.getContent();
            String text = Jsoup.parse(html).text();
            // 2，传递内容进行检测，接收返回值进行判断，如果是消极的话返回报错信息
            try {
                String res = TextCheck.textTag(text);
                if ("Negative".equals(res)) {
                    return Result.error("文章内容包含不当信息，请修改后再发布");
                }else if("请求失败".equals(res)) {
                    return Result.error("内容检测失败，请稍后重试");
                }
            } catch (Exception e) {
                // 记录日志或返回错误信息
                return Result.error("内容检测失败，请稍后重试");
            }
        }
        // 通过用户id获取用户名
        blogs.setUsername(usersMapper.getByUserId(blogs.getUserId()));
        // 上面没有返回说明内容合规
        blogs.setCreatedAt(DateTime.now());
        blogs.setPublishedAt(DateTime.now());
        blogs.setIsDeleted(0);
        int id = blogsMapper.addBlog(blogs);
        BlogsSucessVO blogsSucessVO = new BlogsSucessVO();
        blogsSucessVO.setId(id);
        blogsSucessVO.setTitle(blogs.getTitle());
        blogsSucessVO.setCreateTime(blogs.getCreatedAt());
        return Result.success(blogsSucessVO);
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 获取Blog
     */
    @Override
    public Result<?> getBlog(Integer BlogsId) {
        Blogs blogs =blogsMapper.getBlog(BlogsId);
        return Result.success(blogs);
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 修改Blog
     */
    @Override
    public Result<?> updateBlogs(Blogs blogs) {
        // 如果blog要修改为发布状态，则需要进行审核
        if(blogs.getStatus().equals("published")) {
            // 1,提取内容，去除标签
            String text = Jsoup.parse(blogs.getContent()).text();
            // 2，传递内容进行检测，接收返回值进行判断，如果是消极的话返回报错信息
            try {
                String res = TextCheck.textTag(text);
                if ("Negative".equals(res)) {
                    return Result.error("文章内容包含不当信息，请修改后再发布");
                }else if("请求失败".equals(res)) {
                    return Result.error("内容检测失败，请稍后重试");
                }
            } catch (Exception e) {
                // 记录日志或返回错误信息
                return Result.error("内容检测失败，请稍后重试");
            }
            blogs.setPublishedAt(DateTime.now());
        }
        // 内容合法或不需要发布
        blogs.setUpdatedAt(DateTime.now());
        blogsMapper.updateBlogs(blogs);
        BlogsSucessVO blogsSucessVO = new BlogsSucessVO();
        blogsSucessVO.setId(blogs.getId());
        blogsSucessVO.setTitle(blogs.getTitle());
        blogsSucessVO.setUpdateTime(blogs.getUpdatedAt());
        return Result.success(blogsSucessVO);
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 管理员下架Blog
     */
    @Override
    public Result<?> removeBlog(Integer blogsId) {
        // 获取这条博客
        Blogs blogs = blogsMapper.getBlog(blogsId);

        // 修改状态，被删除人,是否被删除
        blogs.setStatus("draft");
        // 更新Bolg信息
        blogsMapper.updateBlogs(blogs);
        RemoveVO removeVO = new RemoveVO();
        removeVO.setId(blogsId);
        removeVO.setStatus("draft");
        return Result.success(removeVO);
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 分页查询，包含文章名字查询
     */
    @Override
    public PageResult pageQuery(BlogsPageQueryDTO blogsPageQueryDTO) {
        if(blogsPageQueryDTO.getPage() == null) {
            blogsPageQueryDTO.setPage(1);
        }
        if(blogsPageQueryDTO.getPageSize() == null) {
            blogsPageQueryDTO.setPageSize(10);
        }
        PageHelper.startPage(blogsPageQueryDTO.getPage(),blogsPageQueryDTO.getPageSize());
        Page<Blogs> page = blogsMapper.pageQuery(blogsPageQueryDTO);
//        List<Blogs> blogs = page.getResult();
//        List<Blogs> result = new ArrayList<>();
//        for(Blogs blog : blogs){
//            if (blog.getStatus().equals("published")) {
//                result.add(blog);
//            }
//        }
        return new PageResult(page.getTotal(), page.getResult());
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 获取需要渲染的信息
     */
    @Override
    public BlogsShowContext getBlogPDF(Integer blogsId) {
        Blogs blogs = blogsMapper.getBlog(blogsId);
        BlogsShowContext blogsShowContext = new BlogsShowContext();
        // 获取作者的姓名
        String username =usersMapper.getByUserId(blogs.getUserId());
        // 设置需要的信息
        blogsShowContext.setAuthor(username);
        blogsShowContext.setTitle(blogs.getTitle());
        blogsShowContext.setContent(blogs.getContent());
        blogsShowContext.setDate(blogs.getPublishedAt());
        return blogsShowContext;
    }

    @Override
    public Result<?> saveBlogContent(BlogsDraft blogsDraft) {
        // 如果id为空,说明是第一次保存
        if(blogsDraft.getId() == null) {
            // 直接添加数据,并返回主键id
            blogsDraft.setLastSavedAt(DateTime.now());
            int id = blogsDraftMapper.addBlog(blogsDraft);
            return Result.success(id);
        }else {
            // 更新数据
            blogsDraft.setLastSavedAt(DateTime.now());
            blogsDraftMapper.updateBlog(blogsDraft);
            return Result.success(blogsDraft.getId());
        }
    }

    @Override
    public Result<?> getDraft(Integer userId) {
        // 查询当前用户的所有blog
        List<Blogs> list =  blogsMapper.getBlogsByuserId(userId);
        // 遍历blog,判断是否为草稿
        for(Blogs blog : list){
            if(blog.getStatus().equals("draft")){
                return Result.success(blog);
            }
        }
        return Result.success(null);
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-14
     * @Description: 实时保存博客内容为草稿
     */
    @Override
    public Result<?> saveBlogDraft(Blogs blogs) {
        // 如果blogsId为空，说明是第一次保存草稿，直接添加
        if(blogs.getId() == null) {
            // 添加数据
            int blogsId = blogsMapper.addBlog(blogs);
            return Result.success(blogsId);
        }else {
            // 更新数据
            blogsMapper.updateBlogs(blogs);
            return Result.success(blogs.getId());
        }
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-19
     * @Description: 管理员删除博客
     */
    @Override
    public Result<?> deleteBlog(Integer id) {
        // 获取这条博客
        Blogs blogs = blogsMapper.getBlog(id);
        blogs.setStatus("removed");
        blogs.setIsDeleted(1);
        // 更新Bolg信息
        blogsMapper.updateBlogs(blogs);
        RemoveVO removeVO = new RemoveVO();
        removeVO.setId(id);
        removeVO.setStatus("removed");
        return Result.success(removeVO);
    }
}

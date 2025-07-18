package com.blog.service.impl;

import cn.hutool.core.date.DateTime;
import com.blog.dto.UserDTO;
import com.blog.entity.*;
import com.blog.mapper.BlogsDraftMapper;
import com.blog.mapper.BlogsMapper;
import com.blog.mapper.UsersMapper;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.IBlogsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.utils.TextCheck;
import com.blog.utils.UserHolder;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.catalina.User;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    private static final Logger log = LoggerFactory.getLogger(BlogsServiceImpl.class);
    
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
        log.debug("开始创建博客，接收到的数据: {}", blogs);
        
        // 判断标题是否为空
        if(blogs.getTitle() == null || blogs.getTitle().trim().isEmpty()) {
            return Result.error("文章标题不能为空");
        }
        if(blogs.getContent() == null || blogs.getContent().trim().isEmpty()) {
            return Result.error("文章内容不能为空");
        }
        
        // 如果status为null，设置默认值为draft
        if(blogs.getStatus() == null) {
            blogs.setStatus("draft");
        }
        
        // 从当前登录用户上下文中获取用户ID
        UserDTO currentUser = UserHolder.getUser();
        log.debug("当前用户信息: {}", currentUser);
        
        if (currentUser == null) {
            log.warn("UserHolder中没有用户信息");
            return Result.error("用户未登录，请先登录");
        }
        
        if (currentUser.getId() == null) {
            log.warn("当前用户ID为空: {}", currentUser);
            return Result.error("用户信息异常，请重新登录");
        }
        
        // 设置用户ID和用户名
        blogs.setUserId(currentUser.getId());
        blogs.setUsername(currentUser.getUsername());
        
        log.debug("设置博客用户信息: userId={}, username={}", blogs.getUserId(), blogs.getUsername());

        // TODO: 基于测试环境，暂时不进行内容检测
        // 如果是要发布，需要对内容进行检测
        if("published".equals(blogs.getStatus())) {
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
                log.warn("内容检测异常: {}", e.getMessage());
                return Result.error("内容检测失败，请稍后重试");
            }
        }
        
        // 上面没有返回说明内容合规
        blogs.setCreatedAt(DateTime.now());
        blogs.setPublishedAt(DateTime.now());
        blogs.setIsDeleted(0);
        
        log.debug("准备保存博客到数据库: {}", blogs);
        int id = blogsMapper.addBlog(blogs);
        
        BlogsSucessVO blogsSucessVO = new BlogsSucessVO();
        blogsSucessVO.setId(blogs.getId());
        blogsSucessVO.setTitle(blogs.getTitle());
        blogsSucessVO.setCreateTime(blogs.getCreatedAt());
        
        log.debug("博客创建成功，返回结果: {}", blogsSucessVO);
        return Result.success(200, "博客创建成功", blogsSucessVO);
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
        // TODO: 基于测试环境，暂时不进行内容检测
        // 如果blog要修改为发布状态，则需要进行审核
        if(blogs.getStatus().equals("published")) {
//            // 1,提取内容，去除标签
//            String text = Jsoup.parse(blogs.getContent()).text();
//            // 2，传递内容进行检测，接收返回值进行判断，如果是消极的话返回报错信息
//            try {
//                String res = TextCheck.textTag(text);
//                if ("Negative".equals(res)) {
//                    return Result.error("文章内容包含不当信息，请修改后再发布");
//                }else if("请求失败".equals(res)) {
//                    return Result.error("内容检测失败，请稍后重试");
//                }
//            } catch (Exception e) {
//                // 记录日志或返回错误信息
//                return Result.error("内容检测失败，请稍后重试");
//            }
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
        // 修复分页查询时没有设置用户ID的问题
        UserDTO currentUser = UserHolder.getUser();
        System.out.println("当前用户信息: " + currentUser);
        Integer userId = currentUser.getId();
        System.out.println("当前用户ID: " + userId);
        blogsPageQueryDTO.setUserId(userId);

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
    @Override
    public Result<?> withdraw(Integer id){
        // 首先判断id是否存在，以及是否是当前用户的博客
        Blogs blogs = blogsMapper.getBlog(id);
        if (blogs == null) {
            return Result.error("博客不存在或已被删除");
        }
        UserDTO currentUser = UserHolder.getUser();
        // 检查当前用户是否有权限撤回博客
        if (currentUser == null || (!blogs.getUserId().equals(currentUser.getId()) && !"admin".equals(currentUser.getRole()))) {
            return Result.error("您没有权限撤回此博客");
        }
        // 撤回操作则是将状态改为草稿
        blogs.setStatus("draft");
        blogs.setUpdatedAt(DateTime.now());
        blogsMapper.updateBlogs(blogs);
        return Result.success("博客已成功撤回");
    }
}

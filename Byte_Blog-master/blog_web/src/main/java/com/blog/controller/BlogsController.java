package com.blog.controller;


import com.blog.dto.UserDTO;
import com.blog.entity.Blogs;
import com.blog.entity.BlogsPageQueryDTO;
import com.blog.entity.BlogsShowContext;
import com.blog.entity.BlogsSucessVO;
import com.blog.entity.RemoveVO;
import com.blog.mapper.UsersMapper;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.IBlogsService;
import com.blog.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;


import java.io.*;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.thymeleaf.spring5.SpringTemplateEngine;


import java.nio.charset.StandardCharsets;
import java.nio.file.*;

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
@RequestMapping("/api")
public class BlogsController {
    @Autowired
    private IBlogsService blogsService;
    
    @Autowired
    private UsersMapper userMapper;

    private final SpringTemplateEngine templateEngine;
    public BlogsController(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-5
     * @Description: 创建Blog(包括发布,或者存为草稿)
     */
    @PostMapping("/blogs/addBlog")
    public Result<?> addBlog(@RequestBody Blogs blogs){
        log.info("添加Blog：{}" , blogs);
        Result<?> result = blogsService.addBlog(blogs);
        return result;

    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 通过BlogsId获取Blog
     */
    @GetMapping("/blogs/{id}")
    public Result<?> getBlog(@PathVariable Integer id){
        log.info("获取Blog，ID: {}", id);
        Result<?> result = blogsService.getBlog(id);
        return result;
    }
    
    // 添加一个备用路径以兼容旧的前端请求
    @GetMapping("/getBlogs/{id}")
    public Result<?> getBlogCompat(@PathVariable Integer id){
        log.info("获取Blog（兼容路径），ID: {}", id);
        return getBlog(id);
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 修改Blog
     */
    @PutMapping("/updateBlogs/{id}")
    public Result<?> updateBlogs(@PathVariable Integer id, @RequestBody Blogs blogs){
        log.info("更新博客，ID: {}, 数据: {}", id, blogs);
        
        // 设置博客ID
        blogs.setId(id);
        
        // 验证用户权限 - 确保用户只能编辑自己的博客
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("用户未登录，请先登录");
        }
        
        // 获取原博客信息
        Blogs existingBlog = blogsService.getById(id);
        if (existingBlog == null) {
            return Result.error("博客不存在");
        }
        
        // 检查权限：只有博客作者或管理员可以编辑
        boolean isAdmin = "admin".equals(userMapper.getRoleById(currentUser.getId()));
        boolean isOwner = existingBlog.getUserId().equals(currentUser.getId());
        
        if (!isAdmin && !isOwner) {
            return Result.error("无权限编辑此博客");
        }
        
        // 保持原有的用户信息
        blogs.setUserId(existingBlog.getUserId());
        blogs.setUsername(existingBlog.getUsername());
        
        Result<?> result = blogsService.updateBlogs(blogs);
        return result;
    }
    
    // 保留原有的无ID版本以兼容其他调用
    @PutMapping("/updateBlogs")
    public Result<?> updateBlogsCompat(@RequestBody Blogs blogs){
        log.info("更新博客（兼容版本），数据: {}", blogs);
        
        if (blogs.getId() == null) {
            return Result.error("博客ID不能为空");
        }
        
        return updateBlogs(blogs.getId(), blogs);
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 管理员下架Blog
     */
    @PutMapping("/admin/articles/{Id}/withdraw")
    public Result<?> removeBlog(@PathVariable Integer Id , @RequestParam String reason){
        log.info("下架Blog");
        Result<?> result = blogsService.removeBlog(Id);
        return result;
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-19
     * @Description: 管理员删除博客
     */
    @DeleteMapping("/admin/articles/{Id}")
    public Result<?> deleteBlog(@PathVariable Integer Id , @RequestParam String reason){
        log.info("删除Blog");
        Result<?> result = blogsService.deleteBlog(Id);
        return result;
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 管理员分页查询，包含文章名字的模糊查询查询
     */
    @GetMapping("/admin/articles")
    public Result<PageResult> pageBlogs(@RequestBody BlogsPageQueryDTO blogsPageQueryDTO){
        log.info("分页查询");
        PageResult pageResult = blogsService.pageQuery(blogsPageQueryDTO);
        return Result.success(pageResult);
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 导出Blog为PDF
     */
    @PostMapping("/export/{blogsId}")
    public ResponseEntity<byte[]> exportBlog(@PathVariable Integer blogsId) throws IOException {
        log.info("导出Blog为PDF");
        // 查询Blog内容,只保留需要渲染的信息
        BlogsShowContext blogsShowContext = blogsService.getBlogPDF(blogsId);
        // 2. 渲染HTML模板
        Context context = new Context();
        context.setVariable("blog", blogsShowContext);
        String htmlContent = templateEngine.process("blog-pdf-template", context);
        log.info("渲染HTML模板：{}" , htmlContent);
        // HTML -> PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(baos);
        // 注册中文字体
        try (InputStream fontStream = new ClassPathResource("static/fonts/NotoSansSC-Regular.ttf").getInputStream()) {
            builder.useFont(() -> fontStream, "Noto Sans SC");
        } catch (IOException e) {
            throw new RuntimeException("加载字体失败", e);
        }
        builder.run();
        byte[] pdfBytes = baos.toByteArray();

        //  3. 保存 PDF 到磁盘
        String sanitizedTitle = blogsShowContext.getTitle().replaceAll("[^a-zA-Z0-9\\-]", "_"); // 避免非法文件名
        Path savePath = Paths.get("pdf-exports", sanitizedTitle + ".pdf");
        try {
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("保存 PDF 到磁盘失败", e);
        }
        // 返回 PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(blogsShowContext.getTitle() + ".pdf", StandardCharsets.UTF_8)
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /*
     * @Author: kai.hu
     * @Date: 2025-5-14
     * @Description: 实时保存博客内容为草稿
     */
    @PostMapping("/saveBlogContent")
    public Result<?> saveBlogContent(@RequestBody Blogs blogs){
        log.info("保存Blog内容:{}" , blogs);
        Result<?> result = blogsService.saveBlogDraft(blogs);
        return result;
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-15
     * @Description: 点击创建按钮，查询当前用户是否有未发布的草稿
     */
    @GetMapping("/getDraft")
    public Result<?> getDraft(Integer userId) {
        // 查询当前用户是否有未发布的草稿
        Result<?> result = blogsService.getDraft(userId);
        return result;
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-19
     * @Description: 用户获取自己的博客列表
     */
    @GetMapping("/blogs") // 修复：路径应该是 /api/blogs
    public Result<?> getBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status, 
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取用户博客列表");
        
        BlogsPageQueryDTO blogsPageQueryDTO = new BlogsPageQueryDTO();
        blogsPageQueryDTO.setKeyword(keyword);
        blogsPageQueryDTO.setStatus(status);
        blogsPageQueryDTO.setPage(page);
        blogsPageQueryDTO.setPageSize(pageSize);
        
        PageResult pageResult = blogsService.pageQuery(blogsPageQueryDTO);
        return Result.success(pageResult);
    }
    /*
     * @Author: kai.hu
     * @Date: 2025-5-19
     * @Description: 用户端删除博客
     */
    @DeleteMapping("/deleteBlogs/{id}")
    public Result<?> deleteBlog(@PathVariable Integer id) {
        log.info("删除用户博客");
        Result<?> result = blogsService.deleteBlog(id);
        return result;
    }

}


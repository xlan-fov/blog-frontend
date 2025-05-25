package com.blog.controller;


import com.blog.dto.RegisterDTO;
import com.blog.dto.UserDTO;
import com.blog.mapper.UsersMapper;
import com.blog.entity.SensitiveWords;
import com.blog.result.Result;
import com.blog.service.IAdminActionsService;
import com.blog.service.IUsersService;
import com.blog.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
@RequestMapping("/api/admin")
public class AdminActionsController {

    @Autowired
    private IAdminActionsService adminActionsService;
    
    @Autowired
    private UsersMapper userMapper; // 添加UsersMapper依赖

    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {
        
        log.info("获取用户列表：keyword={}, status={}", keyword, status);
        
        // 页码从前端是从1开始的，但数据库通常是从0开始
        return adminActionsService.getUserList(keyword, status);
    }

    @GetMapping("/users/{username}")
    public Result<?> getUserInfo(@PathVariable String username) {
        log.info("获取用户信息：username={}", username);
        return adminActionsService.getUserInfo(username);
    }

    @PostMapping("/users")
    public Result<?> createUser(@RequestBody Map<String, Object> requestBody) {
        String username = (String) requestBody.get("username");
        String password = (String) requestBody.get("password");
        String phone = (String) requestBody.get("phone");
        log.info("创建用户：username={}, password={}, phone={}", username, password, phone);
        return adminActionsService.createUser(username, password, phone);
    }

    @PostMapping("/kickUser")
    public Result<?> kickUser(@RequestBody Map<String, Object> requestBody) {
        Integer userId = Integer.valueOf((String) requestBody.get("id"));
        String reason = (String) requestBody.get("reason");
        log.info("踢出用户：id={}, 原因={}", userId, reason);
        return adminActionsService.kickUser(userId, reason);
    }


    @PostMapping("/users/ban")
    public Result<?> banUser(@RequestBody Map<String, Object> requestBody) {
        String username = (String) requestBody.get("username");
        String reason = (String) requestBody.get("reason");
        log.info("封禁用户：username={}, 原因={}", username, reason);
        return adminActionsService.banUser(username, reason);
    }

    @PostMapping("/users/{username}/ban")
    public Result<?> banUser(@PathVariable String username, @RequestBody Map<String, Object> requestBody) {
        String reason = (String) requestBody.get("reason");
        log.info("封禁用户：username={}, 原因={}", username, reason);
        return adminActionsService.banUser(username, reason);
    }

    @PostMapping("/users/{username}/unban")
    public Result<?> unbanUser(@PathVariable String username, @RequestBody Map<String, Object> requestBody) {
        String reason = (String) requestBody.get("reason");
        log.info("解封用户：username={}, 原因={}", username, reason);
        return adminActionsService.unbanUser(username, reason);
    }


    @GetMapping("/getBlogList")
    public Result<?> getBlogList() {
        log.info("获取博客列表");
        return adminActionsService.getBlogList();
    }

    @GetMapping("/getUserBlogList")
    public Result<?> getUserBlogList(@RequestParam Integer userId) {
        log.info("获取用户博客列表：userId={}", userId);
        return adminActionsService.getUserBlogList(userId);
    }

//    @GetMapping("/articles")
//    public Result<?> getUserArticles(@RequestBody Map<String, Object> requestBody) {
//        String keyword = (String) requestBody.get("keyword");
//        String status = (String) requestBody.get("status");
//        String author = (String) requestBody.get("author");
//        String pageStr = (String) requestBody.get("page");
//        String pageSizeStr = (String) requestBody.get("pageSize");
//        Integer page = pageStr == null ? 1 : Integer.parseInt(pageStr);
//        Integer pageSize = pageSizeStr == null ? 10 : Integer.parseInt(pageSizeStr);
//        log.info("获取用户博客列表：keyword={}, status={}, author={}, page={}, pageSize={}", keyword, status, author, page, pageSize);
//        return adminActionsService.getUserArticles(keyword, status, author, page-1, pageSize);
//    }

    @PostMapping("/articles/{blogId}/publish")
    public Result<?> publishBlog(@PathVariable Integer blogId) {
        log.info("发布博客：id={}", blogId);
        return adminActionsService.publishBlog(blogId);
    }

    @PostMapping("/articles/{blogId}/withdraw")
    public Result<?> withdrawBlog(@PathVariable Integer blogId,@RequestBody Map<String, Object> requestBody) {
        String reason = (String) requestBody.get("reason");
        log.info("撤回博客：id={}, 原因={}", blogId, reason);
        return adminActionsService.withdrawBlog(blogId, reason);
    }

    @DeleteMapping("/articles/{blogId}")
    public Result<?> deleteBlog(@PathVariable Integer blogId, @RequestBody Map<String, Object> requestBody) {
        String reason = (String) requestBody.get("reason");
        log.info("删除博客：id={}, 原因={}", blogId, reason);
        return adminActionsService.deleteBlog(blogId, reason);
    }

    @GetMapping("/getSensitiveWordsList")
    public Result<?> getSensitiveWordsList() {
        log.info("获取敏感词列表");
        return adminActionsService.getSensitiveWordsList();
    }

    @PostMapping("/addSensitiveWord/{sensitiveWord}")
    public Result<?> addSensitiveWord(@PathVariable String sensitiveWord) {
        log.info("添加敏感词：{}", sensitiveWord);
        return adminActionsService.addSensitiveWord(sensitiveWord);
    }
    @PostMapping("/deleteSensitiveWord/{sensitiveWord}")
    public Result<?> deleteSensitiveWord(@PathVariable String sensitiveWord) {
        log.info("删除敏感词：{}", sensitiveWord);
        return adminActionsService.deleteSensitiveWord(sensitiveWord);
    }

    @PostMapping("/addSensitiveWords")
    public Result<?> addSensitiveWords(@RequestBody Map<String, List<String>> requestBody) {
        List<String> sensitiveWords = requestBody.get("sensitiveWord");
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return Result.error("敏感词列表不能为空");
        }
        for (String word : sensitiveWords) {
            log.info("添加敏感词：{}", word);
            adminActionsService.addSensitiveWord(word);
        }
        return Result.success("敏感词列表添加成功");
    }

    @PostMapping("/deleteSensitiveWords")
    public Result<?> deleteSensitiveWords(@RequestBody Map<String, List<String>> requestBody) {
        List<String> sensitiveWords = requestBody.get("sensitiveWord");
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return Result.error("敏感词列表不能为空");
        }
        for (String word : sensitiveWords) {
            log.info("删除敏感词：{}", word);
            adminActionsService.deleteSensitiveWord(word);
        }
        return Result.success("敏感词列表删除成功");
    }

    @GetMapping("/getFailLoginList")
    public Result<?> getFailLoginList() {
        log.info("获取失败登录列表");
        return adminActionsService.getFailLoginList();
    }

    @GetMapping("/anomaly/logins")
    public Result<?> getAnomalyLogins(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username
      ) {

        log.info("获取异常登录列表：startDate={}, endDate={}, username={}", 
                startDate, endDate, username);
        return adminActionsService.getAnomalyLogins(startDate, endDate, username);
    }

    @GetMapping("/getAnomaliesBlogList")
    public Result<?> getAnomaliesBlogList() {
        log.info("获取异常博客列表");
        return adminActionsService.getAnomaliesBlogList();
    }

    @GetMapping("/anomaly/contents")
    public Result<?> getAnomalyContents(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String reason) {

        log.info("获取异常内容列表：startDate={}, endDate={}, username={}, reason={}", 
                startDate, endDate, username, reason);

        return adminActionsService.getAnomalyContents(startDate, endDate, username, reason);
    }

    @GetMapping("/logs")
    public Result<?> getLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String operator,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("获取操作日志：startDate={}, endDate={}, type={}, operator={}, page={}, pageSize={}", 
                startDate, endDate, type, operator, page, pageSize);
        
        return adminActionsService.getAdminActions(startDate, endDate, type, operator, page-1, pageSize);
    }

    @GetMapping("/stats/overview")
    public Result<?> getStatsOverview() {
        log.info("获取统计概览");
        return adminActionsService.getStatsOverview();
    }

    @GetMapping("/stats/users/active")
    public Result<?> getUsersActive(
            @RequestParam(required = false, defaultValue = "week") String period) {
        
        log.info("获取用户活跃度统计，period={}", period);
        return adminActionsService.getUsersActive(period);
    }

    @GetMapping("/stats/contents")
    public Result<?> getContentsStats(
            @RequestParam(required = false, defaultValue = "week") String period) {
        
        log.info("内容发布统计，period={}", period);
        return adminActionsService.getContentsStats(period);
    }

    // 修改这个新接口的路径，避免与BlogsController中的路径冲突
    @GetMapping("/articleslist")
    public Result<?> getArticles(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("获取文章列表：author={}, keyword={}, status={}, page={}, pageSize={}", 
                author, keyword, status, page, pageSize);
                
        // 统一调用已有的博客查询接口
        if (author != null && !author.isEmpty()) {
            // 如果指定了作者，调用获取用户博客列表接口
            return adminActionsService.getUserArticles(keyword, status, author, page-1, pageSize);
        } else {
            // 否则获取所有博客
            return adminActionsService.getBlogList();
        }
    }

    /**
     * 获取管理员个人资料
     */
    @GetMapping("/profile")
    public Result<?> getAdminProfile() {
        log.info("获取管理员个人资料");
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (!userMapper.getRoleById(currentUser.getId()).equals("admin")) {
            return Result.error("无权访问管理员资料");
        }
        
        // 调用新增的服务方法获取管理员资料
        return adminActionsService.getAdminProfile(currentUser.getUsername());
    }
    
    /**
     * 更新管理员个人资料
     */
    @PutMapping("/profile/update")
    public Result<?> updateAdminProfile(@RequestBody Map<String, Object> profileData) {
        log.info("更新管理员个人资料: {}", profileData);
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (!userMapper.getRoleById(currentUser.getId()).equals("admin")) {
            return Result.error("无权更新管理员资料");
        }
        String newUsername = (String) profileData.get("username");
        String phone = (String) profileData.get("phone");
        String bio = (String) profileData.get("bio");
        
        // 调用新增的服务方法更新管理员资料
        return adminActionsService.updateAdminProfile(currentUser.getUsername(), newUsername, phone, bio);
    }
    
    /**
     * 修改管理员密码
     */
    @PutMapping("/profile/password")
    public Result<?> changePassword(@RequestBody Map<String, Object> passwordData) {
        log.info("修改管理员密码");
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (!userMapper.getRoleById(currentUser.getId()).equals("admin")) {
            return Result.error("无权修改管理员密码");
        }
        
        String oldPassword = (String) passwordData.get("oldPassword");
        String newPassword = (String) passwordData.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("密码参数不完整");
        }
        
        // 调用新增的服务方法修改管理员密码
        return adminActionsService.changeAdminPassword(currentUser.getUsername(), oldPassword, newPassword);
    }
}



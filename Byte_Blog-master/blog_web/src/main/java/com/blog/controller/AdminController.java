package com.blog.controller;

import com.blog.dto.UserDTO;
import com.blog.entity.Users;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.IAdminService;
import com.blog.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin-api") // 修改基础路径避免冲突
public class AdminController {

    @Autowired
    private IAdminService adminService;
    
    /**
     * 获取用户列表（管理员权限）
     */
    @GetMapping("/users") // 现在完整路径是 /api/admin-api/users
    public Result<PageResult> getUsers(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "status", required = false) String status) {
        
        log.info("管理员获取用户列表: page={}, pageSize={}, username={}, status={}", page, pageSize, username, status);
        
        // 检查当前用户是否是管理员
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            log.warn("非管理员尝试访问用户列表: {}", currentUser);
            return Result.error("无权访问管理功能");
        }
        
        try {
            PageResult pageResult = adminService.getUserList(page, pageSize, username, status);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员封禁/解封用户
     */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> statusData) {
        
        // 提取状态信息
        Integer status = statusData.get("status") instanceof Integer ? 
                (Integer) statusData.get("status") : 
                Integer.parseInt(statusData.get("status").toString());
        
        log.info("管理员更新用户状态: id={}, status={}", id, status);
        
        // 检查当前用户是否是管理员
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error("无权访问管理功能");
        }
        
        try {
            return adminService.updateUserStatus(id, status);
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Integer id) {
        log.info("管理员删除用户: id={}", id);
        
        // 检查当前用户是否是管理员
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error("无权访问管理功能");
        }
        
        try {
            return adminService.deleteUser(id);
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }
}

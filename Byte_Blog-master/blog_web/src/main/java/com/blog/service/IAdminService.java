package com.blog.service;

import com.blog.result.PageResult;
import com.blog.result.Result;

/**
 * 管理员服务接口
 */
public interface IAdminService {
    
    /**
     * 获取用户列表
     */
    PageResult getUserList(Integer page, Integer pageSize, String username, String status);
    
    /**
     * 更新用户状态（封禁/解封）
     */
    Result<?> updateUserStatus(Integer userId, Integer status);
    
    /**
     * 删除用户
     */
    Result<?> deleteUser(Integer userId);
}

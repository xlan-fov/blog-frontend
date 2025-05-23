package com.blog.service.impl;

import com.blog.entity.Users;
import com.blog.mapper.UsersMapper;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.IAdminService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private UsersMapper usersMapper;
    
    @Override
    public PageResult getUserList(Integer page, Integer pageSize, String username, String status) {
        log.info("查询用户列表: page={}, pageSize={}, username={}, status={}", page, pageSize, username, status);
        
        // 参数校验和默认值设置
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (username != null && !username.trim().isEmpty()) {
            params.put("username", username.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            // 将状态字符串转换为数值
            if ("banned".equals(status)) {
                params.put("isBanned", 1);
            } else if ("normal".equals(status)) {
                params.put("isBanned", 0);
            }
        }
        
        // 设置分页
        PageHelper.startPage(page, pageSize);
        
        // 执行查询
        Page<Users> pageData = usersMapper.pageQueryUsers(params);
        List<Users> records = pageData.getResult();
        
        log.info("查询结果: total={}, size={}", pageData.getTotal(), records.size());
        
        // 转换为PageResult
        return new PageResult(pageData.getTotal(), records);
    }
    
    @Override
    @Transactional
    public Result<?> updateUserStatus(Integer userId, Integer status) {
        log.info("更新用户状态: userId={}, status={}", userId, status);
        
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("无效的用户ID");
        }
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("无效的状态值，应为0(正常)或1(封禁)");
        }
        
        // 查询用户是否存在
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新用户状态
        user.setIsBanned(status);
        int rows = usersMapper.updateUserStatus(userId, status);
        
        if (rows > 0) {
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", userId);
            result.put("status", status);
            
            return Result.success(result);
        } else {
            return Result.error("更新用户状态失败");
        }
    }
    
    @Override
    @Transactional
    public Result<?> deleteUser(Integer userId) {
        log.info("删除用户: userId={}", userId);
        
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("无效的用户ID");
        }
        
        // 查询用户是否存在
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 标记删除用户，不物理删除
        user.setIsDeleted(1);
        int rows = usersMapper.markUserDeleted(userId);
        
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除用户失败");
        }
    }
}

package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.AdminSecret;

/**
 * 管理员密钥服务接口
 */
public interface IAdminSecretService extends IService<AdminSecret> {
    
    /**
     * 根据用户ID获取密钥
     * @param userId 用户ID
     * @return 密钥
     */
    String getSecretByUserId(Integer userId);
}

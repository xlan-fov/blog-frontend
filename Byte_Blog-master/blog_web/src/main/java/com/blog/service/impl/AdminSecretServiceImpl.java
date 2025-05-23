package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.AdminSecret;
import com.blog.mapper.AdminSecretMapper;
import com.blog.service.IAdminSecretService;
import org.springframework.stereotype.Service;

/**
 * 管理员密钥服务实现类
 */
@Service
public class AdminSecretServiceImpl extends ServiceImpl<AdminSecretMapper, AdminSecret> implements IAdminSecretService {
    
    @Override
    public String getSecretByUserId(Integer userId) {
        return baseMapper.selectByUserId(userId);
    }
}

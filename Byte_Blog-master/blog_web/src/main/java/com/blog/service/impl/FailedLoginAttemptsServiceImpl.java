package com.blog.service.impl;

import com.blog.entity.FailedLoginAttempts;
import com.blog.mapper.FailedLoginAttemptsMapper;
import com.blog.service.IFailedLoginAttemptsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Service
public class FailedLoginAttemptsServiceImpl extends ServiceImpl<FailedLoginAttemptsMapper, FailedLoginAttempts> implements IFailedLoginAttemptsService {

}

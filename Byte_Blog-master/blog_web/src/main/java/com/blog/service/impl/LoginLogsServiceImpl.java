package com.blog.service.impl;

import com.blog.entity.LoginLogs;
import com.blog.mapper.LoginLogsMapper;
import com.blog.service.ILoginLogsService;
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
public class LoginLogsServiceImpl extends ServiceImpl<LoginLogsMapper, LoginLogs> implements ILoginLogsService {

}

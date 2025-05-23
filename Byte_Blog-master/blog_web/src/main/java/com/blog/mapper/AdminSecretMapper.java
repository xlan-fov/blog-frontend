package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.AdminSecret;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员密钥 Mapper 接口
 */
@Mapper
public interface AdminSecretMapper extends BaseMapper<AdminSecret> {
    
    /**
     * 根据用户ID查询授权码
     * @param userId 用户ID
     * @return 授权码
     */
    @Select("SELECT secret_code FROM admin_secret WHERE user_id = #{userId}")
    String selectByUserId(Integer userId);
}

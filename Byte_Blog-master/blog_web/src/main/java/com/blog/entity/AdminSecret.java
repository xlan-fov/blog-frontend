package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理员密钥实体类
 */
@Data
@TableName("admin_secret")
public class AdminSecret {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private String secretCode;
}

package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class Users {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String email; // 确保包含email字段
    private String phone;
    private String avatarUrl;
    private String bio;
    private String role;
    private Integer isBanned;
    private Integer isLoggedIn;
    private Integer isDeleted;
    private Integer deletedBy;
    private Date lastLoginTime;
    private Date createdAt;
}

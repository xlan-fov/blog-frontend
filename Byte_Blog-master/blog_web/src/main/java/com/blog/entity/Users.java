package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("users")
public class Users implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;

    @TableField("password_hash")
    private String password;

    private String phone;

    private String avatarUrl;

    private String bio;

    private String role;

    /**
     * 是否被封禁，0-正常，1-封禁
     */
    private Integer isBanned;

    /**
     * 是否已登录，0-未登录，1-已登录
     */
    private Integer isLoggedIn;

    /**
     * 是否被删除，0-正常，1-删除
     */
    private Integer isDeleted;

    /**
     * 删除该用户的操作者ID
     */
    private Integer deletedBy;

    private Date lastLoginTime;

    private Date createdAt;


}

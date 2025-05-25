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

    @TableField("avatar_url")
    private String avatarUrl;

    private String bio;

    private String role;

    /**
     * 是否被封禁，0-正常，1-封禁
     */
    @TableField("is_banned")
    private Integer isBanned;

    /**
     * 是否已登录，0-未登录，1-已登录
     */
    @TableField("is_logged_in")
    private Integer isLoggedIn;

    /**
     * 是否被删除，0-正常，1-删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除该用户的操作者ID
     */
    @TableField("deleted_by")
    private Integer deletedBy;

    @TableField("last_login_time")
    private Date lastLoginTime;

    @TableField("created_at")
    private Date createdAt;


}

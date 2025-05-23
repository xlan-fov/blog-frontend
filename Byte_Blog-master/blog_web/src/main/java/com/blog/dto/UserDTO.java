package com.blog.dto;

import lombok.Data;
import java.util.Date;

@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String phone;
    private String avatarUrl;
    private String bio;
    private String role;
    private Integer isBanned;
    private Integer isLoggedIn;
    private Date lastLoginTime;
    private Date createdAt;
}

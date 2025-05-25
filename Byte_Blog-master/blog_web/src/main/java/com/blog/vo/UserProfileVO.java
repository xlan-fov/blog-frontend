package com.blog.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
public class UserProfileVO {
    //用户ID，用户名，手机号，头像，描述（text），（），是否登录，最后登录时间，创建时间
    private Integer id;
    private String username;
    private String phone;
    private String avatar;
    private String description;
    private Date lastLoginTime;
    private Date registerTime;
}

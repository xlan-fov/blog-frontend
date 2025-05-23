package com.blog.entity;

import lombok.Data;

import java.util.Date;

@Data
public class BlogsSucessVO {
    private Integer id;
    private String title;
    private Date createTime;
    private Date updateTime;
}

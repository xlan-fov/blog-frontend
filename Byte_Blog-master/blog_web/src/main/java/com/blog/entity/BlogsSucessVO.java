package com.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: kai.hu
 * @Date: 2025-05-19-16:50
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogsSucessVO {
    private Integer id;
    private String title;
    private Date createTime;
    private Date updateTime;
}

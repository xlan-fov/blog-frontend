package com.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: kai.hu
 * @Date: 2025-05-07-18:32
 * @Description: 用于接收分页查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogsPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private String title;
    private String username;
    private String status;
    private Integer userId;
}

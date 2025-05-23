package com.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: kai.hu
 * @Date: 2025-05-07-21:07
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogsShowContext {
    private String author;
    private String title;
    private String content;
    private Date date;
}

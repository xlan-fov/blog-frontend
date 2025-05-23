package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: kai.hu
 * @Date: 2025-05-14-20:44
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogsDraft {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Date lastSavedAt;
}

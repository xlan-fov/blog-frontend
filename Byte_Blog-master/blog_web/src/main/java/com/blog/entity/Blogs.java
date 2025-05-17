package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
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
@TableName("blogs")
public class Blogs implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String title;

    private String content;

    private String image;

    private String category;

    private String tags;

    private String status;

    /**
     * 是否被删除，0-正常，1-删除
     */
    private Integer isDeleted;

    /**
     * 删除者ID，若为NULL表示未删除
     */
    private Integer deletedBy;

    private Date createdAt;

    private Date updatedAt;

    private Date publishedAt;


}

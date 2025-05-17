package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class SensitiveWords implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String word;

    /**
     * 是否被删除，0-正常，1-删除
     */
    private Integer isDeleted;

    private Integer deletedBy;

    private Date createdAt;


}

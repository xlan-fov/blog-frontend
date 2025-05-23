package com.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: kai.hu
 * @Date: 2025-05-19-15:32
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveVO {
    private Integer id;
    private String status;
}

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

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    // 如果使用了Lombok的@Data注解，以下getter/setter方法会自动生成
    // 如果没有使用Lombok，需要手动添加以下方法：

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthor() {
        return username;
    }

    public void setAuthor(String author) {
        this.username = author;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

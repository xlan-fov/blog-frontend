package com.blog.mapper;

import com.blog.entity.Blogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.BlogsPageQueryDTO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
public interface BlogsMapper extends BaseMapper<Blogs> {

    /*
     * @Author: kai.hu
     * @Date: 2025-5-5
     * @Description: 添加Blog
     */
    int addBlog(Blogs blogs);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 获取Blog
     */
    @Select("select * from blogs where id = #{BlogsId}")
    Blogs getBlog(Integer BlogsId);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 更新Blog信息
     */
    void updateBlogs(Blogs blogs);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-6
     * @Description: 分页查询
     */
    Page<Blogs> pageQuery(BlogsPageQueryDTO blogsPageQueryDTO);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-15
     * @Description:
     */
    @Select("select * from blogs where user_id = #{userId}")
    List<Blogs> getBlogsByuserId(Integer userId);
//    void addBlog(Blogs blogs);

    @Select("SELECT * FROM blogs WHERE user_id = #{userId}")
    List<Blogs> getBlogListByUserId(int userId);

    @Select("SELECT b.*, u.username " +
            "FROM blogs b " +
            "LEFT JOIN users u ON b.user_id = u.id " +
            "WHERE #{keyword} IS NULL OR (b.title LIKE CONCAT('%', #{keyword}, '%') OR b.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND #{status} IS NULL OR b.status = #{status} " +
            "AND #{username} IS NULL OR b.user_id = (SELECT id FROM users WHERE username = #{username}) " +
            "LIMIT #{page}, #{pageSize}")
    List<Map<String, Object>> getUserArticles(String keyword, String status, String username, Integer page, Integer pageSize);

    @Select("SELECT COUNT(*) FROM blogs WHERE DATE(created_at) = CURDATE()")
    Integer getTodayNewBlogs();

    @Select("SELECT COUNT(*) FROM blogs WHERE status = 'draft'")
    Integer getPendingReviewBlogsCount();

    @Select("SELECT " +
            "CASE " +
            "  WHEN #{period} = 'day' THEN DATE_FORMAT(created_at, '%Y-%m-%d') " +
            "  WHEN #{period} = 'week' THEN CONCAT(DATE_FORMAT(DATE_SUB(created_at, INTERVAL WEEKDAY(created_at) DAY), '%Y-%m-%d'), ' to ', DATE_FORMAT(DATE_ADD(DATE_SUB(created_at, INTERVAL WEEKDAY(created_at) DAY), INTERVAL 6 DAY), '%Y-%m-%d')) " +
            "  WHEN #{period} = 'month' THEN DATE_FORMAT(created_at, '%Y-%m') " +
            "  WHEN #{period} = 'year' THEN DATE_FORMAT(created_at, '%Y') " +
            "END AS time, " +
            "COUNT(*) AS newBlogCount " +
            "FROM blogs " +
            "WHERE is_deleted = 0 " +
            "GROUP BY time " +
            "ORDER BY time DESC")
    List<Map<String, Object>> getNewBlogsByPeriod(String period);

    @Select("SELECT " +
            "CASE " +
            "  WHEN #{period} = 'day' THEN DATE_FORMAT(published_at, '%Y-%m-%d') " +
            "  WHEN #{period} = 'week' THEN CONCAT(DATE_FORMAT(DATE_SUB(published_at, INTERVAL WEEKDAY(published_at) DAY), '%Y-%m-%d'), ' to ', DATE_FORMAT(DATE_ADD(DATE_SUB(published_at, INTERVAL WEEKDAY(published_at) DAY), INTERVAL 6 DAY), '%Y-%m-%d')) " +
            "  WHEN #{period} = 'month' THEN DATE_FORMAT(published_at, '%Y-%m') " +
            "  WHEN #{period} = 'year' THEN DATE_FORMAT(published_at, '%Y') " +
            "END AS time, " +
            "COUNT(*) AS publishedBlogCount " +
            "FROM blogs " +
            "WHERE status = 'published' AND is_deleted = 0 " +
            "GROUP BY time " +
            "ORDER BY time DESC")
    List<Map<String, Object>> getPublishedBlogsByPeriod(String period);
}

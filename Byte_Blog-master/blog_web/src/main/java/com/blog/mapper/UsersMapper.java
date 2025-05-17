package com.blog.mapper;

import com.blog.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

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
@Repository
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    @Select("SELECT id, username, password_hash AS password, phone, avatar_url, bio, role, is_banned, is_logged_in, is_deleted, deleted_by, last_login_time, created_at FROM users WHERE username = #{username}")
    Users selectByUsername(@Param("username") String username);

    @Select("SELECT id, username, password_hash AS password, phone, avatar_url, bio, role, is_banned, is_logged_in, is_deleted, deleted_by, last_login_time, created_at FROM users WHERE phone = #{phone}")
    Users getUserByPhone(@Param("phone") String phone);

    @Select("SELECT role FROM users WHERE id = #{id}")
    String getRoleById(@Param("id") Integer id);

    @Select("SELECT id, username, " +
            "'******' AS password, " +
            "CONCAT(SUBSTRING(phone, 1, 3), '****', SUBSTRING(phone, 8)) AS phone, " +
            "avatar_url, bio, role, is_banned, is_logged_in, is_deleted, last_login_time, created_at " +
            "FROM users " +
            "WHERE (#{keyword} IS NULL OR username LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR (#{status} = 'normal' AND is_banned = 0) OR (#{status} = 'banned' AND is_banned = 1)) " +
            "LIMIT #{page}, #{pageSize}")
    List<Users> getUserList(String keyword, String status, Integer page, Integer pageSize);

    @Select("SELECT u.username, " +
            "u.phone, " +
            "CASE WHEN u.is_banned = 0 THEN 'normal' WHEN u.is_banned = 1 THEN 'banned' END AS status, " +
            "u.created_at AS createTime, " +
            "u.last_login_time AS lastLoginTime, " +
            "(SELECT COUNT(*) FROM blogs b WHERE b.user_id = u.id AND b.is_deleted = 0) AS articleCount " +
            "FROM users u " +
            "WHERE u.username = #{username} ")
    Map<String, Object> getUserInfo(String username);

    @Select("SELECT COUNT(*) " +
            "FROM users " +
            "WHERE DATE(created_at) = CURDATE()")
    Integer getTodayNewUsers();

    @Select("SELECT COUNT(*) " +
            "FROM users " +
            "WHERE DATE(last_login_time) = CURDATE()")
    Integer getTodayActiveUsers();

    @Select("SELECT " +
            "CASE " +
            "  WHEN #{period} = 'day' THEN DATE_FORMAT(last_login_time, '%Y-%m-%d') " +
            "  WHEN #{period} = 'week' THEN CONCAT(DATE_FORMAT(DATE_SUB(last_login_time, INTERVAL WEEKDAY(last_login_time) DAY), '%Y-%m-%d'), ' to ', DATE_FORMAT(DATE_ADD(DATE_SUB(last_login_time, INTERVAL WEEKDAY(last_login_time) DAY), INTERVAL 6 DAY), '%Y-%m-%d')) " +
            "  WHEN #{period} = 'month' THEN DATE_FORMAT(last_login_time, '%Y-%m') " +
            "  WHEN #{period} = 'year' THEN DATE_FORMAT(last_login_time, '%Y') " +
            "END AS time, " +
            "COUNT(*) AS activeUserCount " +
            "FROM users " +
            "WHERE last_login_time IS NOT NULL " +
            "GROUP BY time " +
            "ORDER BY time DESC")
    List<Map<String, Object>> getActiveUsersByPeriod(String period);

    @Select("SELECT " +
            "CASE " +
            "  WHEN #{period} = 'day' THEN DATE_FORMAT(created_at, '%Y-%m-%d') " +
            "  WHEN #{period} = 'week' THEN CONCAT(DATE_FORMAT(DATE_SUB(created_at, INTERVAL WEEKDAY(created_at) DAY), '%Y-%m-%d'), ' to ', DATE_FORMAT(DATE_ADD(DATE_SUB(created_at, INTERVAL WEEKDAY(created_at) DAY), INTERVAL 6 DAY), '%Y-%m-%d')) " +
            "  WHEN #{period} = 'month' THEN DATE_FORMAT(created_at, '%Y-%m') " +
            "  WHEN #{period} = 'year' THEN DATE_FORMAT(created_at, '%Y') " +
            "END AS time, " +
            "COUNT(*) AS newUserCount " +
            "FROM users " +
            "WHERE created_at IS NOT NULL " +
            "GROUP BY time " +
            "ORDER BY time DESC")
    List<Map<String, Object>> getNewUsersByPeriod(String period);
}

package com.blog.mapper;

import com.blog.entity.FailedLoginAttempts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
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
public interface FailedLoginAttemptsMapper extends BaseMapper<FailedLoginAttempts> {



    @Select("SELECT f.*, u.username " +
            "FROM failed_login_attempts f " +
            "LEFT JOIN users u ON f.user_id = u.id " +
            "WHERE f.failure_time BETWEEN #{startDate} AND #{endDate} " +
            "AND (#{username} IS NULL OR u.username = #{username}) " +
            "LIMIT #{page}, #{pageSize}")
    List<Map<String, Object>> getAnomalyLogins(Date startDate, Date endDate, String username, Integer page, Integer pageSize);


    @Select("SELECT COUNT(*) " +
            "FROM failed_login_attempts " +
            "WHERE DATE(failure_time) = CURDATE()")
    Integer getTodayAbnomalLogins();
}

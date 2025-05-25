package com.blog.mapper;

import com.blog.entity.Anomalies;
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
public interface AnomaliesMapper extends BaseMapper<Anomalies> {

    @Select("SELECT a.*, u.username " +
            "FROM anomalies a " +
            "LEFT JOIN users u ON a.user_id = u.id " +
            "WHERE a.created_at BETWEEN #{startDate} AND #{endDate} " +
            "AND (#{username} IS NULL OR u.username = #{username}) " +
            "AND (#{reason} IS NULL OR a.type = #{reason}) ")
    List<Map<String, Object>> getAnomalyContents(Date startDate, Date endDate, String username, String reason);
}

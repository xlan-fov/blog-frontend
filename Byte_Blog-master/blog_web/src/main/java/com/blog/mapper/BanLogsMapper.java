package com.blog.mapper;

import com.blog.entity.BanLogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface BanLogsMapper extends BaseMapper<BanLogs> {

    @Select("SELECT " +
            "bl.action, " +
            "bl.created_at as actionTime, " +
            "bl.reason, " +
            "(SELECT u.username FROM users u WHERE u.id = bl.operator_id) AS operator " +
            "FROM ban_logs bl " +
            "JOIN users u ON bl.user_id = u.id " +
            "WHERE u.username = #{username}")
    List<Map<String, Object>> getBanLogsByUsername(String username);
}

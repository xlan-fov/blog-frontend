package com.blog.mapper;

import com.blog.entity.AdminActions;
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
public interface AdminActionsMapper extends BaseMapper<AdminActions> {

    @Select("SELECT aa.*, u.username AS operator " +
            "FROM admin_actions aa " +
            "LEFT JOIN users u ON aa.admin_id = u.id " +
            "WHERE aa.created_at BETWEEN #{startDate} AND #{endDate} " +
            "AND (#{type} IS NULL OR aa.action_type = #{type}) " +
            "AND (#{operator} IS NULL OR u.username = #{operator}) " +
            "LIMIT #{page}, #{pageSize}")
    List<Map<String,Object>> getAdminActions(Date startDate, Date endDate, String type, String operator, Integer page, Integer pageSize);
}

package com.blog.service;

import com.blog.dto.UserDTO;
import com.blog.entity.AdminActions;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.SensitiveWords;
import com.blog.result.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
public interface IAdminActionsService extends IService<AdminActions> {

    Result<?> getUserList(String keyword, String status, Integer page, Integer pageSize);

    Result<?> getUserInfo(String username);

    Result<?> createUser(String username, String password, String phone);

    Result<?> kickUser(Integer userId,String reason);

    Result<?> banUser(String username,String reason);

    Result<?> unbanUser(String username,String reason);

    Result<?> getBlogList();

    Result<?> getUserBlogList(Integer userId);

    Result<?> getUserArticles(String keyword, String status, String author, Integer page, Integer pageSize);

    Result<?> publishBlog(Integer blogId);

    Result<?> withdrawBlog(Integer blogId,String reason);

    Result<?> deleteBlog(Integer blogId,String reason);

    Result<?> getSensitiveWordsList();

    Result<?> addSensitiveWord(String sensitiveWord);

    Result<?> deleteSensitiveWord(String sensitiveWord);

    Result<?> getFailLoginList();

    Result<?> getAnomalyLogins(String startDate, String endDate, String username,Integer page,Integer pageSize);

    Result<?> getAnomaliesBlogList();

    Result<?> getAnomalyContents(String startDate, String endDate, String username, String reason, Integer page, Integer pageSize);

    Result<?> getAdminActions(String startDate, String endDate, String type, String operator, Integer page, Integer pageSize);

    Result<?> getStatsOverview();

    Result<?> getUsersActive(String period);

    Result<?> getContentsStats(String period);

    /**
     * 获取管理员个人资料
     */
    Result<?> getAdminProfile(String username);

    /**
     * 更新管理员个人资料
     */
    Result<?> updateAdminProfile(String username, String phone, String bio);

    /**
     * 修改管理员密码
     */
    Result<?> changeAdminPassword(String username, String oldPassword, String newPassword);
}

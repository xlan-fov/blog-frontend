package com.blog.service.impl;

import com.blog.dto.UserDTO;
import com.blog.entity.*;
import com.blog.mapper.*;
import com.blog.result.Result;
import com.blog.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.utils.UserHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Service
public class AdminActionsServiceImpl extends ServiceImpl<AdminActionsMapper, AdminActions> implements IAdminActionsService {

    @Autowired
    private UsersMapper userMapper;

    @Autowired
    private BlogsMapper blogsMapper;

    @Autowired
    private SensitiveWordsMapper sensitiveWordsMapper;

    @Autowired
    private FailedLoginAttemptsMapper failedLoginAttemptsMapper;

    @Autowired
    private AnomaliesMapper anomaliesMapper;

    @Autowired
    private BanLogsMapper banLogsMapper;

    @Autowired
    private AdminActionsMapper adminActionsMapper;

    @Autowired
    private IUsersService usersService;

    @Autowired
    private IBlogsService blogsService;

    @Autowired
    private IBanLogsService banLogsService;

    @Autowired
    private IFailedLoginAttemptsService failedLoginAttemptsService;

    @Autowired
    private IAnomaliesService anomaliesService;

    @Autowired
    private ISensitiveWordsService sensitiveWordsService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Result<?> getUserList(String keyword, String status) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        List<Users> users = userMapper.getUserList(keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("total", users.size());
        result.put("list", users);
        return Result.success(result);
    }

    public Result<?> getUserInfo(String username) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if (userMapper.selectByUsername(username) == null) {
            return Result.error("用户不存在");
        }
        Map<String,Object>result = userMapper.getUserInfo(username);
        List<Map<String,Object>> banLogs = banLogsMapper.getBanLogsByUsername(username);
        result.put("banHistory", banLogs);
        return Result.success(result);
    }

    public Result<?> createUser(String username, String password, String phone) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if(username == null || password == null || phone == null) {
            return Result.error("用户名、密码和手机号不能为空");
        }
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(username) != null) {
            return Result.error("用户名已存在");
        }
        // 检查手机号是否已存在
        if (userMapper.getUserByPhone(phone) != null) {
            return Result.error("手机号已存在");
        }
        Users user = new Users();
        user.setUsername(username);

        // String randomPassword = generateRandomPassword(12);
        // 密码加密
        String encodedPwd = passwordEncoder.encode(password);
        user.setPassword(encodedPwd);
        user.setPhone(phone);
        user.setCreatedAt(new Date());
        usersService.save(user);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "CREATE_USER", user.getId(), "USER", "创建用户");
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone());
        result.put("createTime", user.getCreatedAt());
        return Result.success(result);
    }


    public Result<?> kickUser(Integer userId,String reason) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Users user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if(userMapper.getRoleById(userId).equals("admin")) {
            return Result.error("不能踢出管理员");
        }
        user.setIsLoggedIn(0);
        // 用户踢蹬
        userMapper.updateById(user);
        // 记录踢出用户的操作
        recordBanLogs(user.getId(), "kick", currentUser.getId(), reason);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "KICK_USER", user.getId(), "USER", reason);
        return Result.success("用户已被踢出");
    }

    public Result<?> banUser(String username,String reason) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Users user = userMapper.selectByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if(userMapper.getRoleById(user.getId()).equals("admin")) {
            return Result.error("不能封禁管理员");
        }
        // 封禁用户
        user.setIsBanned(1);
        userMapper.updateById(user);
        // 记录封禁用户的操作
        BanLogs banlog = recordBanLogs(user.getId(), "ban", currentUser.getId(), reason);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "BAN_USER", user.getId(), "USER", reason);
        Map<String,Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("status", "banned");
        result.put("banTime", banlog.getCreatedAt());
        return Result.success(result);
    }

    public Result<?> unbanUser(String username,String reason) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Users user = userMapper.selectByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 解封用户
        user.setIsBanned(0);
        userMapper.updateById(user);
        // 记录解封用户的操作
        BanLogs banlog = recordBanLogs(user.getId(), "unban", currentUser.getId(), reason);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "UNBAN_USER", user.getId(), "USER", reason);
        Map<String,Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("status", "normal");
        result.put("banTime", banlog.getCreatedAt());
        return Result.success(result);
    }

    public Result<?> getBlogList() {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        
        // 获取所有博客，包括用户名信息
        List<Map<String, Object>> blogList = blogsMapper.getAllBlogsWithUsername();
        return Result.success(blogList);
    }

    public Result<?> getUserBlogList(Integer userId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Users user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(blogsMapper.getBlogListByUserId(userId));
    }

    public Result<?> getUserArticles(String keyword, String status, String author, Integer page, Integer pageSize) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        
        // 如果没有指定作者，返回所有博客
        if (author == null || author.trim().isEmpty()) {
            List<Map<String,Object>> list = blogsMapper.getAllArticles(keyword, status, page, pageSize);
            Map<String,Object> result = new HashMap<>();
            result.put("total", list.size());
            result.put("list", list);
            return Result.success(result);
        } else {
            // 指定作者的博客
            List<Map<String,Object>> list = blogsMapper.getUserArticles(keyword, status, author, page, pageSize);
            Map<String,Object> result = new HashMap<>();
            result.put("total", list.size());
            result.put("list", list);
            return Result.success(result);
        }
    }

    public Result<?> publishBlog(Integer blogId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Blogs blog = blogsService.getById(blogId);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        // 发布博客
        blog.setStatus("published");
        blog.setPublishedAt(new Date());
        blogsService.updateById(blog);
        // 记录管理员操作
        Map<String,Object> result = new HashMap<>();
        result.put("blogId", blog.getId());
        result.put("status", blog.getStatus());
        result.put("publishTime", blog.getPublishedAt());
        return Result.success(result);
    }

    public Result<?> withdrawBlog(Integer blogId,String reason) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Blogs blog = blogsService.getById(blogId);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        // 撤回博客
        blog.setStatus("draft");
        blogsService.updateById(blog);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "WITHDRAW_BLOG", blogId, "BLOG", reason);
        Map<String,Object> result = new HashMap<>();
        result.put("blogId", blog.getId());
        result.put("status", blog.getStatus());
        return Result.success(result);
    }

    public Result<?> deleteBlog(Integer blogId,String reason) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Blogs blog = blogsService.getById(blogId);
        if (blog == null) {
            return Result.error("博客不存在");
        }
        // 删除博客
        blog.setIsDeleted(1);
        blog.setDeletedBy(currentUser.getId());
        blog.setStatus("removed");
        blogsService.updateById(blog);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "DELETE_BLOG", blogId, "BLOG", reason);
        Map<String,Object> result = new HashMap<>();
        result.put("blogId", blog.getId());
        result.put("status", blog.getStatus());
        return Result.success(result);
    }


    public Result<?> getSensitiveWordsList() {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        return Result.success(sensitiveWordsService.list());
    }

    public Result<?> addSensitiveWord(String sensitiveWord) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        //检查重复
        if (sensitiveWordsMapper.countByWord(sensitiveWord) > 0) {
            SensitiveWords word = sensitiveWordsMapper.selectByWord(sensitiveWord);
            word.setIsDeleted(0);
            word.setDeletedBy(null);
            sensitiveWordsMapper.updateById(word);
            return Result.success("敏感词已存在");
        }
        SensitiveWords newSensitiveWord = new SensitiveWords();
        newSensitiveWord.setWord(sensitiveWord);
        newSensitiveWord.setCreatedAt(new Date());
        sensitiveWordsService.save(newSensitiveWord);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "ADD_SENSITIVE_WORD", newSensitiveWord.getId(), "WORD", "添加敏感词");
        return Result.success("敏感词添加成功");
    }

    public Result<?> deleteSensitiveWord(String sensitiveWord) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        SensitiveWords word = sensitiveWordsMapper.selectByWord(sensitiveWord);
        if (word == null) {
            return Result.error("敏感词不存在");
        }
        // 删除敏感词
        word.setIsDeleted(1);
        word.setDeletedBy(currentUser.getId());
        sensitiveWordsMapper.updateById(word);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "REMOVE_SENSITIVE_WORD", word.getId(), "WORD", "删除敏感词");
        return Result.success("敏感词删除成功");
    }

    public Result<?> editSensitiveWord(String oldWord, String newWord) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        SensitiveWords word = sensitiveWordsMapper.selectByWord(oldWord);
        if (word == null) {
            return Result.error("敏感词不存在");
        }

        if (sensitiveWordsMapper.countByWord(newWord) > 0) {
            SensitiveWords existingWord = sensitiveWordsMapper.selectByWord(newWord);
            if (existingWord.getIsDeleted() == 1) {
                // 如果新敏感词已被删除，则恢复它
                existingWord.setIsDeleted(0);
                existingWord.setDeletedBy(null);
                existingWord.setCreatedAt(new Date());
                sensitiveWordsMapper.updateById(existingWord);
                return Result.success("敏感词编辑成功");
            }
            return Result.error("新敏感词已存在");
        }
        // 修改敏感词
        word.setWord(newWord);
        word.setCreatedAt(new Date());
        sensitiveWordsMapper.updateById(word);
        // 记录管理员操作
        recordAdminActions(currentUser.getId(), "EDIT_SENSITIVE_WORD", word.getId(), "WORD", "编辑敏感词");
        return Result.success("敏感词编辑成功");
    }

    public Result<?> getFailLoginList() {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        return Result.success(failedLoginAttemptsService.list());
    }

    public Result<?> getAnomalyLogins(String startDate, String endDate, String username) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if(startDate == null || endDate == null) {
            return Result.error("日期不能为空");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            List<Map<String,Object>> list=failedLoginAttemptsMapper.getAnomalyLogins(start, end, username);
            Map<String,Object>result = new HashMap<>();
            result.put("total", list.size());
            result.put("list", list);
            return Result.success(result);
        } catch (ParseException e) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    public Result<?> getAnomaliesBlogList() {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        return Result.success(anomaliesService.list());
    }

    public Result<?> getAnomalyContents(String startDate, String endDate, String username, String reason) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if(startDate == null || endDate == null) {
            return Result.error("日期不能为空");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            List<Map<String,Object>> list=anomaliesMapper.getAnomalyContents(start, end, username, reason);
            Map<String,Object>result = new HashMap<>();
            result.put("total", list.size());
            result.put("list", list);
            return Result.success(result);
        } catch (ParseException e) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    public Result<?> getAdminActions(String startDate, String endDate, String type, String operator, Integer page, Integer pageSize) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if(startDate == null || endDate == null) {
            return Result.error("日期不能为空");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            List<Map<String,Object>> list=adminActionsMapper.getAdminActions(start, end, type, operator, page, pageSize);
            Map<String,Object>result = new HashMap<>();
            result.put("total", list.size());
            result.put("list", list);
            return Result.success(result);
        } catch (ParseException e) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    public Result<?> getStatsOverview() {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        Map<String,Object> result = new HashMap<>();
        result.put("totalUsers", userMapper.selectCount(null));
        result.put("totalBlogs", blogsMapper.selectCount(null));
        result.put("totalSensitiveWords", sensitiveWordsMapper.selectCount(null));
        result.put("todayNewUsers", userMapper.getTodayNewUsers());
        result.put("todayNewBlogs", blogsMapper.getTodayNewBlogs());
        result.put("todayActiveUsers", userMapper.getTodayActiveUsers());
        result.put("todayAbnomalLogins", failedLoginAttemptsMapper.getTodayAbnomalLogins());
        result.put("pendingReviews", blogsMapper.getPendingReviewBlogsCount());
        return Result.success(result);
    }

    public Result<?>getUsersActive(String period) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if(period == null) {
            return Result.error("时间段不能为空");
        }
        if(!period.equals("day") && !period.equals("week") && !period.equals("month")&& !period.equals("year")) {
            return Result.error("时间段格式错误");
        }
        List<Map<String, Object>> activeUsers = userMapper.getActiveUsersByPeriod(period);
        List<Map<String, Object>> newUsers = userMapper.getNewUsersByPeriod(period);

        Map<String, Object> result = new HashMap<>();
        result.put("activeUsers", activeUsers);
        result.put("newUsers", newUsers);
        return Result.success(result);
    }

    public Result<?> getContentsStats(String period) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        } else if (userMapper.getRoleById(currentUser.getId()).equals("user")) {
            System.out.println(userMapper.getRoleById(currentUser.getId()));
            return Result.error("没有权限");
        }
        if(period == null) {
            return Result.error("时间段不能为空");
        }
        if(!period.equals("day") && !period.equals("week") && !period.equals("month")&& !period.equals("year")) {
            return Result.error("时间段格式错误");
        }
        List<Map<String, Object>> newBlogs = blogsMapper.getNewBlogsByPeriod(period);
        List<Map<String, Object>> publishedBlogs = blogsMapper.getPublishedBlogsByPeriod(period);

        Map<String, Object> result = new HashMap<>();
        result.put("newBlogs", newBlogs);
        result.put("publishedBlogs", publishedBlogs);
        return Result.success(result);
    }

    private static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private BanLogs recordBanLogs(Integer userId,String actionType,Integer adminId, String reason) {
        BanLogs banLogs = new BanLogs();
        banLogs.setUserId(userId);
        banLogs.setAction(actionType);
        banLogs.setOperatorId(adminId);
        banLogs.setReason(reason);
        // 记录操作时间
        banLogs.setCreatedAt(new Date());
        banLogsService.save(banLogs);
        return banLogs;
    }

    private AdminActions recordAdminActions(Integer adminId,String actionType,Integer targetId,String targetType,String reason)
    {
        AdminActions adminActions = new AdminActions();
        adminActions.setAdminId(adminId);
        adminActions.setActionType(actionType);
        adminActions.setTargetId(targetId);
        adminActions.setTargetType(targetType);
        adminActions.setReason(reason);
        // 记录操作时间
        adminActions.setCreatedAt(new Date());
        save(adminActions);
        return adminActions;
    }

    /**
     * 获取管理员个人资料
     */
    public Result<?> getAdminProfile(String username) {
        Users admin = userMapper.selectByUsername(username);
        if (admin == null) {
            return Result.error("获取管理员资料失败");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("phone", admin.getPhone());
        result.put("avatar", admin.getAvatarUrl());
        result.put("bio", admin.getBio());
        result.put("role", admin.getRole());
        result.put("createdAt", admin.getCreatedAt());
        result.put("lastLoginTime", admin.getLastLoginTime());
        
        return Result.success(result);
    }

    /**
     * 更新管理员个人资料
     */
    public Result<?> updateAdminProfile(String username, String newUsername, String phone, String bio) {
        Users admin = userMapper.selectByUsername(username);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        
        // 更新字段
        if (phone != null) {
            admin.setPhone(phone);
        }
        
        if (bio != null) {
            admin.setBio(bio);
        }
        
        // 保存更新
        int updated = userMapper.updateUserInfo(admin.getId(), newUsername, admin.getPhone(), admin.getBio());
        if (updated > 0) {
            // 返回更新后的信息
            Map<String, Object> result = new HashMap<>();
            result.put("username", newUsername);
            result.put("phone", admin.getPhone());
            result.put("bio", admin.getBio());
            
            return Result.success(result);
        } else {
            return Result.error("更新资料失败");
        }
    }

    /**
     * 修改管理员密码
     */
    public Result<?> changeAdminPassword(String username, String oldPassword, String newPassword) {
        Users admin = userMapper.selectByUsername(username);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return Result.error("原密码不正确");
        }
        
        // 设置新密码
        admin.setPassword(passwordEncoder.encode(newPassword));
        
        // 保存更新
        int updated = userMapper.updateUserPassword(admin.getId(), admin.getPassword());
        if (updated > 0) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("密码修改失败");
        }
    }
}

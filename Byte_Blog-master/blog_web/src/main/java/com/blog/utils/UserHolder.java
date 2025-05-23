package com.blog.utils;

import com.blog.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserHolder {
    private static final ThreadLocal<UserDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 保存用户信息到ThreadLocal
     */
    public static void saveUser(UserDTO user) {
        if (user == null) {
            log.warn("尝试保存null用户到ThreadLocal");
            System.out.println("UserHolder: 尝试保存null用户");
            return;
        }
        
        USER_THREAD_LOCAL.set(user);
        System.out.println("UserHolder: 用户信息已保存到ThreadLocal: " + user);
        log.info("用户信息已保存到ThreadLocal: {}", user);
    }

    /**
     * 获取ThreadLocal中的用户信息
     */
    public static UserDTO getUser() {
        UserDTO user = USER_THREAD_LOCAL.get();
        if (user == null) {
            System.out.println("UserHolder: 获取用户信息失败，ThreadLocal中没有数据");
            log.debug("获取用户信息失败，ThreadLocal中没有数据");
        } else {
            System.out.println("UserHolder: 从ThreadLocal获取用户信息: " + user);
            log.debug("从ThreadLocal获取用户信息: {}", user);
        }
        return user;
    }

    /**
     * 移除ThreadLocal中的用户信息
     */
    public static void removeUser() {
        UserDTO user = USER_THREAD_LOCAL.get();
        if (user != null) {
            System.out.println("UserHolder: 清理ThreadLocal用户信息: " + user.getUsername());
            log.debug("清理ThreadLocal用户信息: {}", user.getUsername());
        }
        USER_THREAD_LOCAL.remove();
    }
}

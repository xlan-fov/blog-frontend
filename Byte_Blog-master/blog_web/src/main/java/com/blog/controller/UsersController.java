package com.blog.controller;


import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.SliderCheckDTO;
import com.blog.result.Result;
import com.blog.service.IUsersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    /*
     * @Author: guoyuran
     * @Date: 2025-5-6
     * @Description: 用户注册登录
     */
    @Autowired
    private IUsersService usersService;

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册：{}");
        return usersService.register(registerDTO);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("用户登录：{}");
        return usersService.login(loginDTO, request);
    }

    // 生成验证码接口 防止自动化批量注册
    // 前端在用户名输入合规后调用
    @GetMapping("/captcha")
    public Result<?> generateCaptcha(@RequestParam String username) {
        log.info("用户注册验证码：{}");
        // 直接生成验证码并返回
        return usersService.captcha(username);
    }

    //滑动模块
    @GetMapping("/slider-image")
    public Result<?> getSliderImage() {
        log.info("登录失败滑动模块：{}");
        return Result.success(usersService.generateSliderImage());
    }

    @PostMapping("/slider-validate")
    public Result<?> validateSlider(@RequestBody SliderCheckDTO sliderCheckDTO) {
        log.info("再次登录滑动模块校验：{}");
        return usersService.validateSlider(sliderCheckDTO);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        log.info("用户退出：{}");
        return Result.success("注销成功");
    }

}


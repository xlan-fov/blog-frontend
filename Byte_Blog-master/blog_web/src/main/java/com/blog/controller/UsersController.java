package com.blog.controller;


import com.blog.dto.*;
import com.blog.result.Result;
import com.blog.service.IUsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @PostMapping("/registerByname")
    public Result<?> registerByname(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册请求: {}", registerDTO); // 修复日志记录，添加参数
        return usersService.registerByname(registerDTO);
    }

    @PostMapping("/loginByname")
    public Result<?> loginByname(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("用户登录：{}");
        return usersService.loginByname(loginDTO, request);
    }

    //@PostMapping("/registerByphone")
    //public Result<?> registerByphone(@Validated @RequestBody PhoneDTO phoneDTO) {
    //    log.info("用户注册：{}");
    //    return usersService.registerByphone(phoneDTO);
    //}

    @PostMapping("/loginByphone")
    public Result<?> loginByphone(@RequestBody PhoneDTO phoneDTO, HttpServletRequest request) {
        log.info("用户登录：{}", phoneDTO);
        return usersService.loginByphone(phoneDTO, request);
    }

    @PostMapping("/adminLogin")
    public Result<?> adminLogin(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("管理员登录：{}", loginDTO);
        return usersService.adminLogin(loginDTO, request);
    }

    /**
     * 发送手机验证码
     * 使用手机号注册时使用
     * 就返回数字
     */
    @PostMapping("/code")
    public Result<?> sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        log.info("手机号注册登录生成验证码：{}", phone);
        return usersService.sendCode(phone);
    }

    // 生成验证码接口 防止自动化批量注册
    // 前端在用户名输入合规后调用
    // 使用username注册时使用，会返回图片
    @GetMapping("/captcha")
    public Result<?> generateCaptcha(@RequestParam(required = false) String captchaId,
                                     @RequestParam(required = false) String username) {
        log.info("用户注册验证码：{}, {}", captchaId, username);
        // 如果captchaId为空，则使用username作为captchaId
        String finalCaptchaId = captchaId;
        if (finalCaptchaId == null || finalCaptchaId.isEmpty()) {
            finalCaptchaId = username + "_" + System.currentTimeMillis();
        }
        // 直接生成验证码并返回
        return usersService.captcha(finalCaptchaId);
    }

    //滑动模块
    @GetMapping("/slider-image")
    public Result<?> getSliderImage() {
        log.info("登录失败滑动模块：生成验证图片");
        return Result.success(usersService.generateSliderImage());
    }

    @PostMapping("/slider-validate")
    public Result<?> validateSlider(@RequestBody SliderCheckDTO sliderCheckDTO) {
        log.info("登录滑动模块校验：{}", sliderCheckDTO);
        return usersService.validateSlider(sliderCheckDTO);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        log.info("用户退出：{}", request.getHeader("Authorization"));
        return usersService.logout(request);
    }

}


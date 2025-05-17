package com.blog.service;

import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.SliderCheckDTO;
import com.blog.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.result.Result;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
public interface IUsersService extends IService<Users> {

    Result<?> register(RegisterDTO registerDTO);
    Result<?> login(LoginDTO loginDTO, HttpServletRequest request);

    Result<?> captcha(String username);

    Result<?> generateSliderImage();

    Result<?> validateSlider(SliderCheckDTO sliderCheckDTO);
}

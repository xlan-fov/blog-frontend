package com.blog.service;

import com.blog.dto.*;
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

    Result<?> registerByname(RegisterDTO registerDTO);
    Result<?> loginByname(LoginDTO loginDTO, HttpServletRequest request);

    Result<?> captcha(String captchaId);

    Result<?> generateSliderImage();

    Result<?> validateSlider(SliderCheckDTO sliderCheckDTO);

    Result<?> logout(HttpServletRequest request);

    //Result<?> registerByphone(PhoneDTO phoneDTO);

    Result<?> loginByphone(PhoneDTO phoneDTO, HttpServletRequest request);

    Result<?> sendCode(String phone);

    Result<?> adminLogin(LoginDTO loginDTO, HttpServletRequest request);

    Result<?> getProfile();

    Result<?> updateProfile(UserProfileDTO userProfileDTO);

    Result<?> updatePassword(UserPasswdChangeDTO userPasswdChangeDTO);
}

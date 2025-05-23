package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
 * @Author: guoyuran
 * @Date: 2025-5-6
 * @Description: 注册格式限制
 */

@Data
public class RegisterDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String captcha;

    private String captchaId;
}

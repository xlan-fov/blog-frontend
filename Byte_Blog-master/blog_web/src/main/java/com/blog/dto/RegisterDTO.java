package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/*
 * @Author: guoyuran
 * @Date: 2025-5-6
 * @Description: 注册格式限制
 */

@Data
public class RegisterDTO {
    @NotBlank
    @Size(min = 6, max = 20, message = "用户名长度应为6-20位")
    private String username;

    @NotBlank
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,16}", message = "密码需包含大小写字母和数字，长度6-16位")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha; // 用户输入的验证码
}

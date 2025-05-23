
package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha; // 用户输入的验证码

    private String captchaId;

    private String secretCode;
}
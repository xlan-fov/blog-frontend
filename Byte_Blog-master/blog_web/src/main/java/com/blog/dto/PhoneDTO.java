package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PhoneDTO {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;
}

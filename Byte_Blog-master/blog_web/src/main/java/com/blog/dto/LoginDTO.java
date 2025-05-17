package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String sliderToken; // 滑块验证码 token

    private Integer sliderX; // 用户拖动的 x 坐标
}

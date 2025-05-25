package com.blog.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserProfileDTO {
    private Integer id;
    private String username;
    private String phone;
    private String description;
}

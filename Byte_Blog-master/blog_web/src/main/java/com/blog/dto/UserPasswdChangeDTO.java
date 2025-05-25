package com.blog.dto;

import lombok.Data;

@Data
public class UserPasswdChangeDTO {
    private Integer id;
    private String oldPassword;
    private String newPassword;
}

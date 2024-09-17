package com.banco.dto;

import lombok.Getter;

@Getter
public class ChangePasswordDto {
    private String newPassword;
    private String confirmPassword;
}

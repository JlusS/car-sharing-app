package com.carrental.carrent.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestDto {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;

    @NotBlank
    @Size(min = MIN_LENGTH, max = MAX_LENGTH)
    @Email
    private String email;
    @NotBlank
    @Size(min = MIN_LENGTH, max = MAX_LENGTH)
    private String password;
}

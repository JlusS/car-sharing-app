package com.carrental.carrent.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserUpdateRequestDto {
    @Email
    private String email;

    private String firstName;

    private String lastName;

    @Length(min = 8, max = 32)
    private String password;
}

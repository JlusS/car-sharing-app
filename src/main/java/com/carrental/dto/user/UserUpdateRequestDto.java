package com.carrental.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserUpdateRequestDto {
    @Email
    private String email;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 255, message = "First name must be between 1 and 255 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 255, message = "Last name must be between 1 and 255 characters")
    private String lastName;

    @Length(min = 8, max = 32)
    private String password;
}

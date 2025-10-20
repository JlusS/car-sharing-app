package com.carrental.carrent.dto.user;

import com.carrental.carrent.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@PasswordMatches
public class UserRegistrationRequestDto {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 32;

    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Length(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH)
    private String password;
    @NotBlank
    @Length(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH)
    private String repeatPassword;
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 255, message = "First name must be between 1 and 255 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 255, message = "Last name must be between 1 and 255 characters")
    private String lastName;
}

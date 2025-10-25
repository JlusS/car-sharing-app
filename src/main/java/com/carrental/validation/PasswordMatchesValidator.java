package com.carrental.validation;

import com.carrental.dto.user.UserRegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements
        ConstraintValidator<PasswordMatches, UserRegistrationRequestDto> {
    @Override
    public boolean isValid(UserRegistrationRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (dto.getPassword() == null || dto.getRepeatPassword() == null) {
            return false;
        }
        return dto.getPassword().equals(dto.getRepeatPassword());
    }
}

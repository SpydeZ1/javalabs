package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || isStrong(value);
    }

    public static boolean isStrong(String password) {
        if (password == null || password.codePointCount(0, password.length()) < MIN_LENGTH) {
            return false;
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return false;
        }
        boolean lower = false;
        boolean upper = false;
        boolean digit = false;
        boolean special = false;
        int index = 0;
        while (index < password.length()) {
            int codePoint = password.codePointAt(index);
            index += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint)) {
                return false;
            }
            if (Character.isLowerCase(codePoint)) {
                lower = true;
            } else if (Character.isUpperCase(codePoint)) {
                upper = true;
            } else if (Character.isDigit(codePoint)) {
                digit = true;
            } else {
                special = true;
            }
        }
        return lower && upper && digit && special;
    }
}

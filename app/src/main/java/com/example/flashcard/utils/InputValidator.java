package com.example.flashcard.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class InputValidator {

    // 1. Kiểm tra Email hợp lệ
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 2. Kiểm tra độ dài mật khẩu (Ví dụ: Tối thiểu 6 ký tự)
    public static boolean isPasswordLongEnough(String password) {
        return password != null && password.length() >= 6;
    }

    // 3. (Nâng cao) Kiểm tra mật khẩu có chứa số hay không?
    public static boolean hasNumber(String password) {
        // Regex kiểm tra xem chuỗi có chứa ít nhất 1 số (0-9) không
        return Pattern.compile(".*[0-9].*").matcher(password).matches();
    }

    // 4. Kiểm tra xem 2 mật khẩu có khớp nhau không
    public static boolean isPasswordMatch(String pass1, String pass2) {
        return pass1 != null && pass1.equals(pass2);
    }
}
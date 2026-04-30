package com.attendance.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-\\s]{7,15}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone == null || phone.isBlank() || PHONE.matcher(phone.trim()).matches();
    }

    public static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean isStrongPassword(String pw) {
        return pw != null && pw.length() >= 6;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (!isNotBlank(value)) throw new IllegalArgumentException(fieldName + " is required.");
        return value.trim();
    }
}

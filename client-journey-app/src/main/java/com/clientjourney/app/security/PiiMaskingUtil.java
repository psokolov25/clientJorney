package com.clientjourney.app.security;

public final class PiiMaskingUtil {
    private PiiMaskingUtil() {}

    public static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.substring(0, 1) + "***" + email.substring(at - 1);
    }

    public static String maskPhone(String phone) {
        if (phone.length() <= 4) return "****";
        return phone.substring(0, 2) + "***" + phone.substring(phone.length() - 2);
    }
}

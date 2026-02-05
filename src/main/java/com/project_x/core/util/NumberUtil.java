package com.project_x.core.util;

public class NumberUtil {
    public static String normalizePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("\\s+", "");
        if (phoneNumber.startsWith("0")) {
            // Convert to +234 format
            return "+234" + phoneNumber.substring(1);
        }
        return phoneNumber;
    }
}

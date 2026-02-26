package com.project_x.core.util;

import java.util.Random;

public class NumberUtil {
    public static String normalizePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("\\s+", "");
        if (phoneNumber.startsWith("0")) {
            // Convert to +234 format
            return "+234" + phoneNumber.substring(1);
        }
        return phoneNumber;
    }

    public static String generateNumericOTP() {
        Random random = new Random();
        int otpLength = 5;
        StringBuilder otpBuilder = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            int digit = random.nextInt(10);
            otpBuilder.append(digit);
        }

        return otpBuilder.toString();
    }
}

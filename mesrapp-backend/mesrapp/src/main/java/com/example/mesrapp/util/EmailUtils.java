package com.example.mesrapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,6}$");

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(String.valueOf(EMAIL_PATTERN));
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

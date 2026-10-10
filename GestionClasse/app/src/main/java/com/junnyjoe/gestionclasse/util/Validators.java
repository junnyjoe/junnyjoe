package com.junnyjoe.gestionclasse.util;

import java.util.regex.Pattern;

/** Règles de validation des formulaires (Java pur, donc testables sans appareil). */
public final class Validators {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 .\\-()]+$");

    private Validators() { }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isEmailValid(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    /** Au moins 6 chiffres, uniquement des chiffres, espaces, +, -, ., parenthèses. */
    public static boolean isPhoneValid(String s) {
        if (isBlank(s) || !PHONE.matcher(s.trim()).matches()) return false;
        int digits = 0;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) digits++;
        }
        return digits >= 6;
    }
}

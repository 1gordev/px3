package com.id.px3.utils.sec;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Encodes a plain password.
     *
     * @param plainPassword the plain password to encode
     * @return the encoded password
     */
    public static String encodePassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    /**
     * Checks if a plain password matches an encoded one.
     *
     * @param plainPassword the plain password
     * @param encodedPassword the encoded password
     * @return true if the passwords match, false otherwise
     */
    public static boolean matchPassword(String plainPassword, String encodedPassword) {
        return encoder.matches(plainPassword, encodedPassword);
    }
}

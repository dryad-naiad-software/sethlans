package com.dryadandnaiad.sethlans.config;

/**
 * Created Mario Estrella on 2/16/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String SECRET = "HJjdU8XKpkE8Iuo9N0fW";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private Constants() {
    }
}

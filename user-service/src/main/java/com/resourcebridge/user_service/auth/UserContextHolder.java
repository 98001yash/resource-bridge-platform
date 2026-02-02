package com.resourcebridge.user_service.auth;

public final class UserContextHolder {

    private static final ThreadLocal<Long> USER_ID =
            new ThreadLocal<>();

    private static final ThreadLocal<String> ROLE =
            new ThreadLocal<>();

    private static final ThreadLocal<Boolean> VERIFIED =
            new ThreadLocal<>();

    private UserContextHolder() {}

    /* ========== SET ========== */

    static void setUserId(Long id) {
        USER_ID.set(id);
    }

    static void setRole(String role) {
        ROLE.set(role);
    }

    static void setVerified(Boolean verified) {
        VERIFIED.set(verified);
    }

    /* ========== GET ========== */

    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    public static String getCurrentRole() {
        return ROLE.get();
    }

    public static Boolean isVerified() {
        return VERIFIED.get();
    }

    /* ========== CLEAR ========== */

    static void clear() {
        USER_ID.remove();
        ROLE.remove();
        VERIFIED.remove();
    }
}

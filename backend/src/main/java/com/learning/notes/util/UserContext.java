package com.learning.notes.util;

public class UserContext {

    // 默认用户ID（用于公开访问）
    public static final Long DEFAULT_USER_ID = 1L;
    public static final String DEFAULT_USERNAME = "admin";

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

    public static void setCurrentUser(Long userId, String username) {
        currentUserId.set(userId);
        currentUsername.set(username);
    }

    /**
     * 获取当前用户ID，如果未登录则返回默认用户ID
     */
    public static Long getCurrentUserId() {
        Long userId = currentUserId.get();
        return userId != null ? userId : DEFAULT_USER_ID;
    }

    /**
     * 获取当前用户名，如果未登录则返回默认用户名
     */
    public static String getCurrentUsername() {
        String username = currentUsername.get();
        return username != null ? username : DEFAULT_USERNAME;
    }

    public static void clear() {
        currentUserId.remove();
        currentUsername.remove();
    }

    public static boolean isAuthenticated() {
        return currentUserId.get() != null;
    }
}

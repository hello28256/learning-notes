package com.learning.notes.controller;

import com.learning.notes.service.UserService;
import com.learning.notes.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 更新头像
    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestBody Map<String, String> request) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("未登录"));
        }

        try {
            String avatar = request.get("avatar");
            if (avatar == null || avatar.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("头像地址不能为空"));
            }

            Long userId = UserContext.getCurrentUserId();
            userService.updateAvatar(userId, avatar);
            return ResponseEntity.ok(createSuccessResponse("头像更新成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 更新用户名
    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> request) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("未登录"));
        }

        try {
            String newUsername = request.get("username");
            if (newUsername == null || newUsername.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名不能为空"));
            }

            Long userId = UserContext.getCurrentUserId();
            userService.updateUsername(userId, newUsername);
            return ResponseEntity.ok(createSuccessResponse("用户名修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 更新密码
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("未登录"));
        }

        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("当前密码和新密码不能为空"));
            }

            Long userId = UserContext.getCurrentUserId();
            userService.updatePassword(userId, currentPassword, newPassword);
            return ResponseEntity.ok(createSuccessResponse("密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 获取当前用户信息
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(createErrorResponse("未登录"));
        }

        try {
            Long userId = UserContext.getCurrentUserId();
            Map<String, Object> userInfo = userService.getUserInfo(userId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}

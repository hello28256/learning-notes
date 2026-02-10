package com.learning.notes.controller;

import com.learning.notes.service.UserService;
import com.learning.notes.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // 路径将是 /api/auth/*，因为 application.yml 中配置了 context-path: /api

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名和密码不能为空"));
            }

            userService.register(username, password, email);
            return ResponseEntity.ok(createSuccessResponse("注册成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名和密码不能为空"));
            }

            Map<String, Object> result = userService.login(username, password);
            return ResponseEntity.ok(createSuccessResponse("登录成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

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

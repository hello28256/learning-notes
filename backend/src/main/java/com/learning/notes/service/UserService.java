package com.learning.notes.service;

import com.learning.notes.entity.User;
import com.learning.notes.repository.UserRepository;
import com.learning.notes.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = User.builder()
                .username(username)
                .password(password) // 实际项目中应该加密
                .email(email)
                .build();

        return userRepository.save(user);
    }

    public Map<String, Object> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户名或密码错误");
        }

        User user = userOpt.get();
        if (!password.equals(user.getPassword())) { // 实际项目中应该比对加密后的密码
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());

        return result;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 更新头像
    public void updateAvatar(Long userId, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatar);
        userRepository.save(user);
    }

    // 更新用户名
    public void updateUsername(Long userId, String newUsername) {
        // 检查新用户名是否已存在
        if (userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("用户名已存在");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    // 更新密码
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证当前密码
        if (!currentPassword.equals(user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    // 获取用户信息
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("avatar", user.getAvatar());

        return userInfo;
    }
}

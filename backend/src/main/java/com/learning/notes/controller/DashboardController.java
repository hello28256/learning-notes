package com.learning.notes.controller;

import com.learning.notes.service.DashboardService;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = UserContext.getCurrentUserId();
        return ResponseEntity.ok(dashboardService.getDashboardData(userId));
    }
}

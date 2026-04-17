package com.bigfly.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.bigfly.dto.LoginRequest;
import com.bigfly.entity.User;
import com.bigfly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Sa-Token 登录认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 测试登录
     */
    @SaIgnore
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用UserService进行登录验证
            User user = userService.login(request.getUsername(), request.getPassword());
            
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", Map.of(
                "tokenInfo", tokenInfo,
                "userInfo", user
            ));
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        StpUtil.logout();
        result.put("code", 200);
        result.put("message", "退出成功");
        return result;
    }

    /**
     * 查询登录状态
     */
    @SaIgnore
    @GetMapping("/isLogin")
    public Map<String, Object> isLogin() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("isLogin", StpUtil.isLogin());
        result.put("loginId", StpUtil.getLoginIdDefaultNull());
        return result;
    }

    /**
     * 查询 Token 信息
     */
    @GetMapping("/tokenInfo")
    public Map<String, Object> tokenInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", StpUtil.getTokenInfo());
        return result;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/currentUser")
    public Map<String, Object> currentUser() {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getCurrentUser();
            result.put("code", 200);
            result.put("data", Map.of(
                "user", user,
                "roles", userService.getCurrentUserRoles(),
                "menus", userService.getCurrentUserMenus(),
                "perms", userService.getCurrentUserPerms()
            ));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 检查权限
     */
    @GetMapping("/checkPermission")
    public Map<String, Object> checkPermission(@RequestParam String permission) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("hasPermission", userService.hasPermission(permission));
        return result;
    }

    /**
     * 检查角色
     */
    @GetMapping("/checkRole")
    public Map<String, Object> checkRole(@RequestParam String roleCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("hasRole", userService.hasRole(roleCode));
        return result;
    }

}

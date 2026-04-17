package com.bigfly.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限测试控制器
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    /**
     * 公开接口，不需要登录
     */
    @SaIgnore
    @GetMapping("/public")
    public Map<String, Object> publicApi() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是公开接口");
        return result;
    }

    /**
     * 需要登录的接口
     */
    @GetMapping("/login-required")
    public Map<String, Object> loginRequired() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "需要登录才能访问");
        return result;
    }

    /**
     * 需要 user:list 权限的接口
     */
    @SaCheckPermission("user:list")
    @GetMapping("/user/list")
    public Map<String, Object> userList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "用户列表数据");
        return result;
    }

    /**
     * 需要 user:add 权限的接口
     */
    @SaCheckPermission("user:add")
    @PostMapping("/user/add")
    public Map<String, Object> addUser() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "添加用户成功");
        return result;
    }

    /**
     * 需要 admin 角色的接口
     */
    @SaCheckRole("admin")
    @GetMapping("/admin/only")
    public Map<String, Object> adminOnly() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "仅管理员可访问");
        return result;
    }

    /**
     * 类级别权限控制 - 所有方法都需要 menu:list 权限
     */
    @SaCheckPermission("menu:list")
    @RestController
    @RequestMapping("/api/menu")
    @RequiredArgsConstructor
    public static class MenuController {

        @GetMapping("/list")
        public Map<String, Object> list() {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "菜单列表");
            return result;
        }

        @PostMapping("/add")
        public Map<String, Object> add() {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "添加菜单");
            return result;
        }
    }
}

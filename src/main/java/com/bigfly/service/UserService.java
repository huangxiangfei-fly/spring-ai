package com.bigfly.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigfly.entity.Menu;
import com.bigfly.entity.Role;
import com.bigfly.entity.User;
import com.bigfly.mapper.MenuMapper;
import com.bigfly.mapper.RoleMapper;
import com.bigfly.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        // 验证用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码（实际应该使用加密比对）
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 验证用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 登录成功，将用户ID存入Sa-Token
        StpUtil.login(user.getId());

        // 将用户信息存入Session
        StpUtil.getSession().set("userInfo", user);

        // 加载用户角色和权限到Session
        loadUserPermissions(user.getId());

        return user;
    }

    /**
     * 加载用户权限信息到Session
     */
    private void loadUserPermissions(Long userId) {
        // 查询用户角色
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        StpUtil.getSession().set("roles", roles);

        // 查询用户菜单
        List<Menu> menus = menuMapper.selectMenusByUserId(userId);
        StpUtil.getSession().set("menus", menus);

        // 查询用户权限标识
        List<String> perms = menuMapper.selectPermsByUserId(userId);
        StpUtil.getSession().set("perms", perms);
    }

    /**
     * 获取当前登录用户信息
     */
    public User getCurrentUser() {
        // 从Sa-Token Session中获取用户信息
        Object userInfo = StpUtil.getSession().get("userInfo");
        if (userInfo != null) {
            return (User) userInfo;
        }

        // 如果Session中没有，从数据库查询
        Long userId = StpUtil.getLoginIdAsLong();
        return userMapper.selectById(userId);
    }

    /**
     * 获取当前用户角色列表
     */
    @SuppressWarnings("unchecked")
    public List<Role> getCurrentUserRoles() {
        List<Role> roles = (List<Role>) StpUtil.getSession().get("roles");
        if (roles == null) {
            Long userId = StpUtil.getLoginIdAsLong();
            roles = roleMapper.selectRolesByUserId(userId);
            StpUtil.getSession().set("roles", roles);
        }
        return roles;
    }

    /**
     * 获取当前用户菜单列表
     */
    @SuppressWarnings("unchecked")
    public List<Menu> getCurrentUserMenus() {
        List<Menu> menus = (List<Menu>) StpUtil.getSession().get("menus");
        if (menus == null) {
            Long userId = StpUtil.getLoginIdAsLong();
            menus = menuMapper.selectMenusByUserId(userId);
            StpUtil.getSession().set("menus", menus);
        }
        return menus;
    }

    /**
     * 获取当前用户权限标识列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getCurrentUserPerms() {
        List<String> perms = (List<String>) StpUtil.getSession().get("perms");
        if (perms == null) {
            Long userId = StpUtil.getLoginIdAsLong();
            perms = menuMapper.selectPermsByUserId(userId);
            StpUtil.getSession().set("perms", perms);
        }
        return perms;
    }

    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(String permission) {
        List<String> perms = getCurrentUserPerms();
        return perms.contains(permission);
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public boolean hasRole(String roleCode) {
        List<Role> roles = getCurrentUserRoles();
        return roles.stream().anyMatch(role -> role.getRoleCode().equals(roleCode));
    }

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}

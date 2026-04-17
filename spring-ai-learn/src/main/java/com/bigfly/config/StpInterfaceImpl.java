package com.bigfly.config;

import cn.dev33.satoken.stp.StpInterface;
import com.bigfly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 自定义权限加载接口实现类
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 从数据库查询用户权限标识列表
        return userService.getCurrentUserPerms();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从数据库查询用户角色编码列表
        return userService.getCurrentUserRoles().stream()
                .map(role -> role.getRoleCode())
                .collect(Collectors.toList());
    }
}

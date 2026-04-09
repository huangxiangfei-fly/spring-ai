package com.bigfly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigfly.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}

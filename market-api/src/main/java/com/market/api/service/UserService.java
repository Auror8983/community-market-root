package com.market.api.service;

import com.market.common.domain.Factory;
import com.market.common.domain.SysUser;
import java.util.List;

public interface UserService {

    // --- 用户相关 ---

    // 登录
    SysUser login(String username, String password);

    // 获取用户列表 (支持按角色筛选)
    List<SysUser> getUserList(String role);

    // 根据ID获取用户详情
    SysUser getUserById(String userId);

    // 添加用户 (ID自动生成)
    void addUser(SysUser user);

    // 更新用户
    void updateUser(SysUser user);

    // 删除用户
    void deleteUser(String userId);

    // --- 厂家相关 ---

    // 获取厂家列表
    List<Factory> getFactoryList();

    // 添加或更新厂家 (ID自动生成)
    void addFactory(Factory factory);

    // 删除厂家
    void deleteFactory(String factoryId);
}
package com.market.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.market.api.service.UserService;
import com.market.common.domain.Factory;
import com.market.common.domain.SysUser;
import com.market.user.mapper.FactoryMapper;
import com.market.user.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FactoryMapper factoryMapper;

    @Override
    public SysUser login(String username, String password) {
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.eq("username", username);
        query.eq("password", password);
        return userMapper.selectOne(query);
    }

    @Override
    public List<SysUser> getUserList(String role) {
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            query.eq("role", role);
        }
        return userMapper.selectList(query);
    }

    @Override
    public SysUser getUserById(String userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void addUser(SysUser user) {
        // ID 自动生成逻辑: Max + 1
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            String maxIdStr = userMapper.selectMaxUserId();
            long nextId = 1; // 默认从 1 开始
            if (maxIdStr != null) {
                try {
                    nextId = Long.parseLong(maxIdStr) + 1;
                } catch (NumberFormatException e) {
                    nextId = 1000; // 容错处理
                }
            }
            // 格式化为 4 位字符串 (如 0005)
            user.setUserId(String.format("%04d", nextId));
        }

        // 密码默认处理
        if(user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }

        userMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(String userId) {
        userMapper.deleteById(userId);
    }

    @Override
    public List<Factory> getFactoryList() {
        return factoryMapper.selectList(null);
    }

    @Override
    public void addFactory(Factory factory) {
        // 兼容: 如果传入了ID且数据库存在，则视为更新
        if (factory.getFactoryId() != null && !factory.getFactoryId().isEmpty()) {
            Factory existing = factoryMapper.selectById(factory.getFactoryId());
            if (existing != null) {
                factoryMapper.updateById(factory);
                return;
            }
        }

        // ID 自动生成逻辑: Max + 1
        if (factory.getFactoryId() == null || factory.getFactoryId().isEmpty()) {
            String maxIdStr = factoryMapper.selectMaxFactoryId();
            long nextId = 1;
            if (maxIdStr != null) {
                try {
                    nextId = Long.parseLong(maxIdStr) + 1;
                } catch (NumberFormatException e) {
                    nextId = 1000;
                }
            }
            factory.setFactoryId(String.format("%04d", nextId));
        }

        factoryMapper.insert(factory);
    }

    @Override
    public void deleteFactory(String factoryId) {
        factoryMapper.deleteById(factoryId);
    }
}
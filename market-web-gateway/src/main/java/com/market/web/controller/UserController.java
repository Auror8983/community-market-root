package com.market.web.controller;

import com.market.api.service.UserService;
import com.market.common.domain.Factory;
import com.market.common.domain.SysUser;
import com.market.common.result.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @DubboReference
    private UserService userService;

    // --- 用户相关 ---

    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody SysUser user) {
        SysUser loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser != null) {
            return Result.success(loginUser);
        }
        return Result.error("用户名或密码错误");
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list(@RequestParam(required = false) String role) {
        return Result.success(userService.getUserList(role));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SysUser user) {
        try {
            userService.addUser(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return Result.success();
    }

    // --- 厂家相关 ---

    @GetMapping("/factory/list")
    public Result<List<Factory>> factoryList() {
        return Result.success(userService.getFactoryList());
    }

    @PostMapping("/factory/add")
    public Result<Void> addFactory(@RequestBody Factory factory) {
        try {
            userService.addFactory(factory);
            return Result.success();
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/factory/delete/{id}")
    public Result<Void> deleteFactory(@PathVariable String id) {
        userService.deleteFactory(id);
        return Result.success();
    }
}
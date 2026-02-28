package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.User;

public interface UserService extends IService<User> {
    /**
     * 分页查询所有用户
     */
    IPage<User> getUserList(Page<User> page, String role);

    /**
     * 禁用或启用用户相关
     */
    void updateUserStatus(Integer userId, Integer status);
}

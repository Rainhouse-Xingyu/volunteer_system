package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.User;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public IPage<User> getUserList(Page<User> page, String role) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 排除管理员自己
        queryWrapper.ne(User::getRole, "admin");
        
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(User::getRole, role);
        }
        
        // 隐藏密码字段
        queryWrapper.select(User.class, info -> !info.getColumn().equals("password"));
        
        return this.page(page, queryWrapper);
    }

    @Override
    public void updateUserStatus(Integer userId, Integer status) {
        if (status != 0 && status != 1) {
            throw new ServiceException("状态值非法");
        }
        
        User user = this.getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        if ("admin".equals(user.getRole())) {
            throw new ServiceException("无法修改管理员状态");
        }
        
        user.setStatus(status);
        this.updateById(user);
    }
}

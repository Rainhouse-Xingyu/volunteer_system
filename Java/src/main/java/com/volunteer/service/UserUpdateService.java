package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.UserUpdate;

public interface UserUpdateService extends IService<UserUpdate> {

    void submitUpdate(Integer userId, String type, Object original, Object modified);

    void auditUpdate(Integer id, Integer adminId, Integer status, String reason);
}

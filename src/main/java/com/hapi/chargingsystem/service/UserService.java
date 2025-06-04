package com.hapi.chargingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hapi.chargingsystem.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    User getByUsername(String username);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByUsername(String username);
}

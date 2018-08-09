package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.IUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService implements IUserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public User selectByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username,password);
    }
}

package com.zkjl.posite_cloud.service;


import com.zkjl.posite_cloud.domain.pojo.User;

public interface IUserService {

    User selectByUsernameAndPassword(String username, String password);
}

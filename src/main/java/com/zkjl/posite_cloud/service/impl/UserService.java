package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.IUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;

@Service
public class UserService implements IUserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public User selectByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username,password);
    }

    @Override
    public User create(User user) {
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        if(loginUser.getPermission().contains("create1")){
            user.setJobLevel("group");
        }else if(loginUser.getPermission().contains("create2")){
            user.setJobLevel("normal");
        }
        user.setCreationTime(Calendar.getInstance().getTime());
        return userRepository.save(user);
    }

    @Override
    public Boolean delete(String id) {
        boolean flag = false;
        try {
            userRepository.deleteById(id);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}

package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.dao.LogRepository;
import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.dto.LogDTO;
import com.zkjl.posite_cloud.domain.pojo.Log;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private LogRepository logRepository;

    @Override
    public User selectByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public User create(User user) {
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (loginUser.getPermission().contains("create1")) {
            user.setJobLevel("group");
        } else if (loginUser.getPermission().contains("create2")) {
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

    @Override
    public PageImpl<Log> findLog(LogDTO log) {
        List<Log> logs = logRepository.findByUsername(log.getUsername());
        List<Log> collect = logs.stream().filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(log.getBeginDate())) {
                if (action.getCretionTime().compareTo(log.getBeginDate()) >= 0) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(log.getEndDate())) {
                if (action.getCretionTime().compareTo(log.getBeginDate()) <= 0) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(log.getUsername())) {
                if (action.getUsername().equals(log.getUsername())) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).collect(Collectors.toList());
        return (PageImpl<Log>) PageUtil.pageBeagin(collect.size(), log.getPageNum(), log.getPageSize(), collect);
    }
}

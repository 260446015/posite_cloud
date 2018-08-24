package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.dao.LogRepository;
import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.dto.LogDTO;
import com.zkjl.posite_cloud.domain.dto.UserDTO;
import com.zkjl.posite_cloud.domain.pojo.Log;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private LogRepository logRepository;
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public User selectByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public User create(User user) throws CustomerException {
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        /*if (loginUser.getPermission().contains("create1")) {
            user.setJobLevel("group");
        } else if (loginUser.getPermission().contains("create2")) {
            user.setJobLevel("normal");
        }*/
        User check = userRepository.findByUsername(user.getUsername());
        if(null != check){
            throw new CustomerException("用户名已存在");
        }
        if (StringUtils.isBlank(user.getJobLevel())) {
            return null;
        }
        user.setDomain(loginUser.getDomain());
        user.setCreator(loginUser.getUsername());
        if (user.getIfEnable() == null) {
            user.setIfEnable(false);
        }
        user.setCreationTime(DateUtils.getFormatString(Calendar.getInstance().getTime()));
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
        if (logs.size() == 0) {
            return null;
        }
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

    @Override
    public PageImpl<User> findUser(UserDTO userDTO, User login) {
        String domain = login.getDomain();
        List<User> users = userRepository.findByDomain(domain);
        users.remove(login);
        if (users.size() == 0) {
            return null;
        }
        List<User> collect = users.stream().filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(userDTO.getUsername())) {
                if (action.getUsername().equals(userDTO.getUsername())) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(userDTO.getName())) {
                if (action.getName().equals(userDTO.getName())) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(userDTO.getBeginDate())) {
                if (action.getCreationTime().compareTo(userDTO.getBeginDate()) >= 0) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(userDTO.getEndDate())) {
                if (action.getCreationTime().compareTo(userDTO.getEndDate()) < 0) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).collect(Collectors.toList());
        return (PageImpl<User>) PageUtil.pageBeagin(collect.size(), userDTO.getPageNum(), userDTO.getPageSize(), collect);
    }

    @Override
    public User findUserById(String id) {
        Optional<User> byId = userRepository.findById(id);
        return byId.orElse(null);
    }

    @Override
    public boolean deleteLog(String id) {
        boolean flag = false;
        try {
            logRepository.deleteById(id);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean enable(String id, Boolean ifEnable) {
        boolean flag = false;
        User userById = findUserById(id);
        if (userById == null) {
            return false;
        }
        userById.setIfEnable(ifEnable);
        try {
            userRepository.save(userById);
            flag = true;
        } catch (Exception e) {
            logger.error("更改用户状态失败", e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean updateSentiment(String[] msg) {
        try {
            String join = StringUtils.join(msg, ",");
            User user = (User) SecurityUtils.getSubject().getPrincipal();
            user.setSentiment(join);
            userRepository.save(user);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean updateUser(User user) throws CustomerException {
        Optional<User> byId = userRepository.findById(user.getId());
        User check = byId.orElse(null);
        if(check == null){
            throw new CustomerException("用户不存在");
        }
        if(!check.getPassword().equals(user.getPassword())){
            throw new CustomerException("密码错误");
        }
        if(!StringUtils.isBlank(user.getName())){
            check.setName(user.getName());
        }
        if(!StringUtils.isBlank(user.getPassword())){
            check.setPassword(user.getPassword());
        }
        if(null != user.getAge()){
            check.setAge(user.getAge());
        }
        if(!StringUtils.isBlank(user.getMobile())){
            check.setMobile(user.getMobile());
        }
        if(!StringUtils.isBlank(user.getEmail())){
            check.setEmail(user.getEmail());
        }
        if(!StringUtils.isBlank(user.getDepartment())){
            check.setDepartment(user.getDepartment());
        }
        if(!StringUtils.isBlank(user.getJob())){
            check.setJob(user.getJob());
        }
        try {
            userRepository.save(check);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

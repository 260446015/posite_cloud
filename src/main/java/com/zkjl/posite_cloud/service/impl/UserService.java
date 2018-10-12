package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.ArrayList;
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
    public User create(User user, String userid) throws CustomerException {
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        user.setCreator(loginUser.getUsername());
        user.setSearchCount(2000);
        user.setTotalSerachCount(2000);
        if (StringUtils.isBlank(userid)) {
            if (loginUser.getJobLevel().equals("admin")) {
                user.setJobLevel("group");
            } else if (loginUser.getJobLevel().equals("group")) {
                user.setJobLevel("normal");
            }
        } else {
            user.setJobLevel("normal");
            Optional<User> byId = userRepository.findById(userid);
            User user1 = byId.orElse(null);
            user.setCreator(user1.getUsername());
        }
        User check = userRepository.findByUsername(user.getUsername());
        if (null != check) {
            throw new CustomerException("用户名已存在");
        }
//        if (!RegUtil.checkPass(user.getPassword())) {
//            throw new CustomerException("密码格式不对");
//        }
        if (StringUtils.isBlank(user.getJobLevel())) {
            return null;
        }
        user.setDomain(loginUser.getDomain());
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
    public PageImpl<JSONObject> findUser(UserDTO userDTO, User login) {
        String creator = login.getUsername();
        List<User> users = userRepository.findByCreator(creator);
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
        List<JSONObject> result = new ArrayList<>();

        if (login.getJobLevel().equals("admin")) {
            collect.forEach(action -> {
                JSONObject data = new JSONObject();
                data.put("data", action);
                data.put("element", null);
                List<User> byCreator = userRepository.findByCreator(action.getUsername());
                if (byCreator.size() != 0) {
                    JSONArray nextArr = new JSONArray();
                    byCreator.forEach(action2 -> {
                        JSONObject secondData = new JSONObject();
                        secondData.put("data", action2);
                        secondData.put("element", null);
                        List<User> byCreator1 = userRepository.findByCreator(action2.getUsername());
                        if (byCreator1.size() != 0) {
                            secondData.put("element", byCreator);
                        }
                        nextArr.add(secondData);
                    });
                    data.put("element", nextArr);
                }
                result.add(data);
            });
        } else if (login.getJobLevel().equals("group")) {
            collect.forEach(action -> {
                JSONObject data = new JSONObject();
                data.put("data", action);
                data.put("element", null);
                List<User> byCreator = userRepository.findByCreator(action.getUsername());
                if (byCreator.size() != 0) {
                    data.put("element", byCreator);
                }
                result.add(data);
            });
        } else {
            collect.forEach(action -> {
                JSONObject data = new JSONObject();
                data.put("data", action);
                result.add(data);
            });
        }

        return (PageImpl<JSONObject>) PageUtil.pageBeagin(result.size(), userDTO.getPageNum(), userDTO.getPageSize(), result);
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
        if (check == null) {
            throw new CustomerException("用户不存在");
        }
        if (!StringUtils.isBlank(user.getName())) {
            check.setName(user.getName());
        }
        if (null != user.getAge()) {
            check.setAge(user.getAge());
        }
        if (!StringUtils.isBlank(user.getMobile())) {
            check.setMobile(user.getMobile());
        }
        if (!StringUtils.isBlank(user.getEmail())) {
            check.setEmail(user.getEmail());
        }
        if (!StringUtils.isBlank(user.getDepartment())) {
            check.setDepartment(user.getDepartment());
        }
        if (!StringUtils.isBlank(user.getJob())) {
            check.setJob(user.getJob());
        }
        if (!StringUtils.isBlank(user.getImage())) {
            check.setImage(user.getImage());
        }
        try {
            userRepository.save(check);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean updatePassword(String oldPassword, String newPassword, String id) throws CustomerException {
        Optional<User> byId = userRepository.findById(id);
        User check = byId.orElse(null);
        if (null == check) {
            throw new CustomerException("用户不存在");
        }
        if (!check.getPassword().equals(oldPassword)) {
            throw new CustomerException("密码错误");
        } else {
//            if (RegUtil.checkPass(newPassword)) {
            check.setPassword(newPassword);
//            } else {
//                throw new CustomerException("密码格式不对");
//            }
        }
        try {
            userRepository.save(check);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

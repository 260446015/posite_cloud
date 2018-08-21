package com.zkjl.posite_cloud.service;


import com.zkjl.posite_cloud.domain.dto.LogDTO;
import com.zkjl.posite_cloud.domain.dto.UserDTO;
import com.zkjl.posite_cloud.domain.pojo.Log;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.exception.CustomerException;
import org.springframework.data.domain.PageImpl;

public interface IUserService {

    User selectByUsernameAndPassword(String username, String password);

    User create(User user) throws CustomerException;

    Boolean delete(String id);

    PageImpl<Log> findLog(LogDTO log);

    PageImpl<User> findUser(UserDTO userDTO, User login);

    User findUserById(String id);

    boolean deleteLog(String id);

    boolean enable(String id, Boolean ifEnable);
}

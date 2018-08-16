package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.domain.dto.LogDTO;
import com.zkjl.posite_cloud.domain.dto.UserDTO;
import com.zkjl.posite_cloud.domain.pojo.Log;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/14 19:08
 **/
@RestController
@RequestMapping(value = "api")
public class UserController extends BaseController {
    @Resource
    private IUserService userService;

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 创建更改用户
     *
     * @param user
     * @return
     */
    @PostMapping(value = "create")
    @RequiresPermissions(value = {"create1", "create2"}, logical = Logical.OR)
    @SystemControllerLog(description = "创建用户")
    @ApiOperation(value = "创建更改用户")
    public ApiResult create(@RequestBody User user) {
        User result;
        try {
            result = userService.create(user);
        } catch (Exception e) {
            logger.error("创建用户失败!", e.getMessage());
            return error("创建用户失败!");
        }
        return success(result);
    }

    @GetMapping(value = "delete")
    @ApiOperation(value = "根据id删除用户")
    public ApiResult delete(String id) {
        Boolean flag;
        try {
            flag = userService.delete(id);
        } catch (Exception e) {
            logger.error("删除用户失败!", e.getMessage());
            return error("删除用户失败!");
        }
        return success(flag);
    }

    /**
     * 查询日志
     *
     * @param log
     * @return
     */
    @PostMapping(value = "findLog")
    public ApiResult findLog(@RequestBody LogDTO log) {
        PageImpl<Log> result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            log.setUsername(username);
            result = userService.findLog(log);
        } catch (Exception e) {
            logger.error("查询日志失败!", e.getMessage());
            return error("查询日志失败!");
        }
        return successPages(result);
    }

    /**
     * 删除日志
     *
     * @param id
     * @return
     */
    @GetMapping(value = "deleteLog")
    public ApiResult deleteLog(String id) {
        boolean flag;
        try {
            flag = userService.deleteLog(id);
        } catch (Exception e) {
            logger.error("删除日志失败!", e.getMessage());
            return error("删除日志失败!");
        }
        return success(flag);
    }

    /**
     * 查询用户
     *
     * @param userDTO
     * @return
     */
    @PostMapping(value = "findUser")
    @RequiresRoles(value = "admin")
    public ApiResult findUser(@RequestBody UserDTO userDTO) {
        PageImpl<User> result;
        User login;
        try {
            login = this.getCurrentUser();
            result = userService.findUser(userDTO, login);
        } catch (Exception e) {
            logger.error("查询日志失败!", e.getMessage());
            return error("查询日志失败!");
        }
        return successPages(result);
    }

    /**
     * 根据id查询用户
     */
    @GetMapping(value = "findUserById")
    public ApiResult findUserById(String id){
        User result;
        try {
            result = userService.findUserById(id);
        } catch (Exception e) {
            logger.error("查询日志失败!", e.getMessage());
            return error("查询日志失败!");
        }
        return success(result);
    }


}

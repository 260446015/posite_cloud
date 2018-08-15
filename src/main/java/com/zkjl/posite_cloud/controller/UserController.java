package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * @param user
     * @return
     */
    @PostMapping(value = "create")
    @RequiresPermissions(value = {"create1","create2"},logical = Logical.OR)
    @ApiOperation(value = "创建更改用户")
    public ApiResult create(@RequestBody User user){
        User result;
        try {
            result = userService.create(user);
        } catch (Exception e) {
            logger.error("创建用户失败!",e.getMessage());
            return error("创建用户失败!");
        }
        return success(result);
    }

    @GetMapping(value = "delete")
    @ApiOperation(value = "根据id删除用户")
    public ApiResult delete(String id){
        Boolean flag;
        try {
            flag = userService.delete(id);
        } catch (Exception e) {
            logger.error("删除用户失败!",e.getMessage());
            return error("删除用户失败!");
        }
        return success(flag);
    }


}

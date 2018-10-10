package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.common.ViKeyJavaObj;
import com.zkjl.posite_cloud.domain.dto.LogDTO;
import com.zkjl.posite_cloud.domain.dto.UserDTO;
import com.zkjl.posite_cloud.domain.pojo.Log;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.domain.vo.UserVo;
import com.zkjl.posite_cloud.service.IFileService;
import com.zkjl.posite_cloud.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private IFileService fileService;

    /**
     * 创建更改用户
     *
     * @param user
     * @return
     */
    @PostMapping(value = "create")
    @RequiresRoles(value = "admin")
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
        if (result == null) {
            return error("创建用户需指定用户级别");
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
        if (result == null) {
            return successPagesNull(result);
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
            logger.error("查询用户失败!", e.getMessage());
            return error("查询用户失败!");
        }
        if (result == null) {
            return successPagesNull(result);
        }
        return successPages(result);
    }

    /**
     * 根据id查询用户
     */
    @GetMapping(value = "findUserById")
    public ApiResult findUserById(String id) {
        User result;
        try {
            result = userService.findUserById(id);
        } catch (Exception e) {
            logger.error("查询日志失败!", e.getMessage());
            return error("查询日志失败!");
        }
        return success(result);
    }

    /**
     * 启用或禁用
     */
    @GetMapping(value = "enable")
    @ApiOperation(value = "启用或禁用")
    public ApiResult enable(String id, Boolean ifEnable) {
        boolean flag;
        try {
            flag = userService.enable(id, ifEnable);
        } catch (Exception e) {
            logger.error("启用或禁用失败!", e.getMessage());
            return error("启用或禁用失败!");
        }
        return success(flag);
    }

    /**
     * 更新用户舆情设置
     */
    @GetMapping(value = "updateSentiment")
    @ApiOperation(value = "更新用户舆情设置")
    public ApiResult updateSentiment(@RequestParam(value = "msg[]") String[] msg) {
        boolean flag;
        try {
            flag = userService.updateSentiment(msg);
        } catch (Exception e) {
            logger.error("更新用户舆情设置失败!", e.getMessage());
            return error("更新用户舆情设置失败!");
        }
        return success(flag);
    }

    /**
     * 查询用户舆情设置
     */
    @GetMapping(value = "findSentiment")
    @ApiOperation(value = "查询用户舆情设置")
    public ApiResult findSentiment() {
        String[] result;
        try {
            result = this.getCurrentUser().getSentiment().split(",");
        } catch (Exception e) {
            return success(null);
        }
        return success(result);
    }

    /**
     * 查询登录用户
     */
    @GetMapping(value = "getLoginUser")
    @ApiOperation(value = "查询登录用户")
    public ApiResult getLoginUser() {
        UserVo result = new UserVo();
        try {
            User user = this.getCurrentUser();
            BeanUtils.copyProperties(user,result);
        } catch (Exception e) {
            return success(null);
        }
        return success(result);
    }

    /**
     * 个人中心更新用户信息
     */
    @PostMapping(value = "updateUser")
    public ApiResult updateUser(@RequestBody User user) {
        boolean flag;
        try {
            flag = userService.updateUser(user);
        } catch (Exception e) {
            logger.error("更新用户失败!" + e.getMessage());
            return error("更新用户失败!" + e.getMessage());
        }
        return success(flag);
    }

    /**
     * 个人中心修改密码
     */
    @GetMapping(value = "updatePassword")
    public ApiResult updatePassword(String oldPassword, String newPassword, String id) {
        boolean flag;
        try {
            flag = userService.updatePassword(oldPassword, newPassword, id);
        } catch (Exception e) {
            logger.error("修改密码失败!" + e.getMessage());
            return error("修改密码失败!" + e.getMessage());
        }
        return success(flag);
    }

    /**
     * 图片上传
     *
     * @param req
     * @throws Exception
     */
    @RequestMapping(value = "/uploadImg", method = RequestMethod.POST)
    @ResponseBody
    public List<Map<String, Object>> upload(HttpServletRequest req, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;
        List<Map<String, Object>> mapList = new ArrayList<>();
        List multipartFiles = multipartRequest
                .getFiles("file");// 得到所有的文件
        for (int i = 0; i < multipartFiles.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            MultipartFile multipartFile = (MultipartFile) multipartFiles.get(i);
            if (multipartFile.getSize() <= 0L) {
                return null;
            }
            String fileUrl = fileService.uploadImg(req, multipartFile);
            map.put("url", fileUrl);
            mapList.add(map);
        }
        return mapList;
    }
    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }


    public static void main(String[] args){
        int[]	dwCount=new int[1];
        short	Index = 0;
        int[]	HID= new int[1];
        long retval=0;
        ViKeyJavaObj viKeyJavaObj=new ViKeyJavaObj();
        // 查找加密狗
        retval = viKeyJavaObj.ViKeyFind(dwCount);
        if (retval == viKeyJavaObj.VIKEY_SUCCESS)
        {
            System.out.println("系统中找到ViKey加密狗数量:"+dwCount[0]);
        }
        else
        {
            System.out.println("系统中没有找到ViKey加密狗");
            return;
        }

        Index = 0;
        //获取加密狗硬件ID
        retval = viKeyJavaObj.ViKeyGetHID(Index, HID);
        if (retval == viKeyJavaObj.VIKEY_SUCCESS)
        {
            System.out.println("获取加密狗的硬件ID:"+HID[0]);
        }
        else
        {
            System.out.println("获取加密狗类型错误");
            return;
        }
        //System.out.println(System.getProperty("java.library.path"));
       /* byte [] k=strToByteArray("zkjluser");

        int a =viKeyJavaObj.ViKeyUserLogin((short) 0, k);

        System.out.println(a);*/
    }

}

package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/9 11:08
 **/
@RestController
@RequestMapping(value = "api")
public class ApiController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(ApiController.class);
    @Resource
    private IApiService apiService;

    @PostMapping(value = "createJob")
    public ApiResult createRedisJob(@RequestBody JobDTO jobDTO) {
        JobDTO jobInfo;
        try {
            String username = getCurrentUser().getUsername();
            if (StringUtils.isEmpty(username)) {
                throw new CustomerException("用户名为空");
            }
            jobDTO.setUsername(username);
            jobInfo = apiService.createJob(jobDTO);
        } catch (Exception e) {
            log.error("创建任务失败，失败原因:" + e.getMessage());
            return error("创建任务失败，失败原因:" + e.getMessage());
        }
        return success(jobInfo);
    }
    @PostMapping(value = "updateJob")
    public ApiResult updateJob(@RequestBody JobDTO jobDTO) {
        Boolean flag;
        try {
            String username = getCurrentUser().getUsername();
            if (StringUtils.isEmpty(username)) {
                throw new CustomerException("用户名为空");
            }
            jobDTO.setUsername(username);
            flag = apiService.updateJob(jobDTO);
        } catch (Exception e) {
            log.error("创建任务失败，失败原因:" + e.getMessage());
            return error("创建任务失败，失败原因:" + e.getMessage());
        }
        return success(flag);
    }

    /**
     * 获取数据
     * @return
     */
    @GetMapping(value = "retrieveData")
    public ApiResult retrieveData(){
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.realTimeData(username);
        } catch (Exception e) {
            log.error("获取数据实时进度出错!",e.getMessage());
            return error("获取数据实时进度出错!");
        }
        return success(result);
    }

    /**
     * 获取发展阶段分析
     * @return
     */
    @GetMapping(value = "development")
    public ApiResult development(){
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.developmentData(username);
        } catch (Exception e) {
            log.error("获取发展阶段分析出错!",e.getMessage());
            return error("获取发展阶段分析出错!");
        }
        return success(result);
    }

    /**
     * 获取实时注册信息
     * @return
     */
    @GetMapping(value = "timeRegist")
    public ApiResult realTimeRegist(Integer pageNum,Integer pageSize){
        PageImpl<JobInfo> result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.realTimeRegist(username,pageNum,pageSize);
        } catch (Exception e) {
            log.error("获取实时注册信息出错!",e.getMessage());
            return error("获取实时注册信息出错!");
        }
        return successPages(result);
    }

}

package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping(value = "createRedisJob")
    public ApiResult createRedisJob(@RequestBody JobDTO jobDTO) {
        JobInfo jobInfo;
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
}

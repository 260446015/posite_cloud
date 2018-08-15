package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IApiService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @GetMapping(value = "createJob")
    public ApiResult createRedisJob(String datas) {
        JobDTO jobInfo;
        try {
            String username = getCurrentUser().getUsername();
            if (StringUtils.isEmpty(username)) {
                throw new CustomerException("用户名为空");
            }
            JobDTO jobDTO = new JobDTO();
            jobDTO.setUsername(username);
            jobDTO.setDatas(datas);
            jobDTO.setStatus("start");
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
     *
     * @return
     */
    @GetMapping(value = "retrieveData")
    @ApiOperation(value = "获取数据")
    public ApiResult retrieveData() {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.realTimeData(username);
        } catch (Exception e) {
            log.error("获取数据实时进度出错!", e.getMessage());
            return error("获取数据实时进度出错!");
        }
        return success(result);
    }

    /**
     * 获取发展阶段分析
     *
     * @return
     */
    @GetMapping(value = "development")
    @ApiOperation(value = "获取发展阶段分析")
    public ApiResult development() {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.developmentData(username);
        } catch (Exception e) {
            log.error("获取发展阶段分析出错!", e.getMessage());
            return error("获取发展阶段分析出错!");
        }
        return success(result);
    }

    /**
     * 获取实时注册信息
     *
     * @return
     */
    @GetMapping(value = "timeRegist")
    @ApiOperation(value = "获取实时注册信息")
    public ApiResult realTimeRegist() {
        List<JSONObject> result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.realTimeRegist(username);
        } catch (Exception e) {
            log.error("获取实时注册信息出错!", e.getMessage());
            return error("获取实时注册信息出错!");
        }
        return success(result);
    }

    /**
     * 获取redis任务信息
     *
     * @return
     */
    @GetMapping(value = "listJob")
    @ApiOperation(value = "获取任务信息")
    public ApiResult listRedisJob() {
        List<JobinfoVO> result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.listJob(username);
        } catch (Exception e) {
            log.error("获取redis任务信息出错!", e.getMessage());
            return error("获取任务信息出错!");
        }
        return success(result);
    }

    /**
     * 获取舆情信息接口
     */
    @PostMapping(value = "sentiment")
    @ApiOperation(value = "获取舆情信息接口")
    public ApiResult sentiment(@RequestBody SentimentDTO sentimentDTO) {
        JSONObject result;
        try {
            result = apiService.getSentiment(sentimentDTO);
        } catch (Exception e) {
            log.error("获取舆情信息接口出错!", e.getMessage());
            return error("获取舆情信息接口出错!");
        }
        return success(result);
    }

    /**
     * 按任务id查询
     */
    @GetMapping(value = "searchByTaskid")
    @ApiOperation(value = "按任务id查询")
    public ApiResult searchByTaskid(String taskId, Integer pageNum, Integer pageSize, String msg) {
        JSONObject result;
        try {
            result = apiService.searchByTaskid(taskId, pageNum, pageSize, msg);
        } catch (Exception e) {
            log.error("按任务id查询出错!", e.getMessage());
            return error("按任务id查询出错!");
        }
        return success(result);
    }

    /**
     * 按任务id查询进度
     */
    @GetMapping(value = "searchByTaskidPlan")
    @ApiOperation(value = "按任务id查询进度")
    public ApiResult searchByTaskidPlan(String taskId) {
        JSONObject result;
        try {
            result = apiService.searchByTaskidPlan(taskId);
        } catch (Exception e) {
            log.error("按任务id查询进度出错!", e.getMessage());
            return error("按任务id查询进度出错!");
        }
        return success(result);
    }


}

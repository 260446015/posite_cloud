package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IApiService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    @SystemControllerLog(description = "添加采集-手动录入")
    public ApiResult createRedisJob(String datas, HttpServletRequest request) {
        JobDTO jobInfo;
        String taskname = request.getParameter("taskname");
        try {
            String username = getCurrentUser().getUsername();
            if (StringUtils.isEmpty(username)) {
                throw new CustomerException("用户名为空");
            }
            JobDTO jobDTO = new JobDTO();
            jobDTO.setUsername(username);
            jobDTO.setTaskname(taskname);
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
            log.error("获取占比分析出错!", e.getMessage());
            return error("获取占比分析出错!");
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
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = apiService.listAllJob(username);
        } catch (Exception e) {
            log.error("获取redis任务信息出错!" + e.getMessage());
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
        String userSentiment;
        try {
            userSentiment = this.getCurrentUser().getSentiment();
            result = apiService.getSentiment(sentimentDTO, userSentiment);
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

    /**
     * 批量删除任务
     *
     * @param ids
     * @return
     */
    @GetMapping(value = "deleteBatch")
    public ApiResult deleteBatch(@RequestParam(value = "ids[]") String[] ids) {
        boolean flag;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            flag = apiService.deleteBatch(ids, username);
        } catch (Exception e) {
            log.error("批量删除任务出错!", e.getMessage());
            return error("批量删除任务进度出错!");
        }
        return success(flag);
    }

    /**
     * 任务指定接口
     */
    @PostMapping(value = "assignment",params = {"taskid","userid"})
    public ApiResult taskAssignment(@RequestParam(value = "taskid[]") String[] taskid,
                                    @RequestParam(value = "userid[]") String[] userid) {
        boolean result;
        try {
            if(taskid.length == 0 || userid.length == 0){
                return error("请确保传入的参数长度不为0");
            }
            result = apiService.taskAssignment(taskid, userid);
        } catch (Exception e) {
            log.error("任务指定接口出错!", e.getMessage());
            return error("任务指定接口出错!");
        }
        return success(result);
    }

}

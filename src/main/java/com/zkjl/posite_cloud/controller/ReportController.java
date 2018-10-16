package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.domain.vo.RedistaskVO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IReportService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yindawei
 * @date 2018/8/14 16:10
 **/
@RestController
@RequestMapping(value = "api")
public class ReportController extends BaseController {

    @Resource
    private IReportService reportService;

    private static Logger logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * 获取报告信息
     * 根据传递参数进行正则判断是否为手机号
     *
     * @param mobile
     * @return
     */
    @GetMapping(value = "report")
    @SystemControllerLog(description = "生成报告")
    @ApiOperation(value = "获取报告信息")
    public ApiResult report(String mobile) {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = reportService.report(mobile, username);
        } catch (Exception e) {
            logger.error("获取报告信息出错!", e.getMessage());
            return error("获取报告信息出错!");
        }
        return success(result);
    }

    /**
     * 按照历史报告传递id，进行报告生成
     */
    @GetMapping(value = "reportById")
    @SystemControllerLog(description = "按照id生成报告")
    @ApiOperation(value = "按照id生成报告")
    public ApiResult reportById(String id) {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = reportService.reportById(id, username);
        } catch (Exception e) {
            logger.error("按照id生成报告!" + e.getMessage());
            return error("按照id生成报告异常");
        }
        return success(result);
    }

    /**
     * 查看历史报告
     */
    @GetMapping(value = "findReportHistory")
    public ApiResult findReportHistory(String taskid, Integer pageNum, Integer pageSize) {
        PageImpl<RedistaskVO> result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = reportService.findReportHistory(taskid, pageNum, pageSize, username);
        } catch (Exception e) {
            e.printStackTrace();
            return error("查询历史报告失败");
        }
        if (result == null) {
            return successPagesNull(null);
        }
        return successPages(result);
    }

    @GetMapping(value = "reportByMobileBatch")
    public ApiResult reportByMobileBatch(@RequestParam(value = "ids[]") String[] ids, Boolean ifSelectAll) {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = reportService.reportByMobileBatch(ids, username, ifSelectAll);
        } catch (Exception e) {
            logger.error("获取手机号批量报告信息出错!" + e.getMessage());
            return error("获取报告信息出错!");
        }
        return success(result);
    }

    @GetMapping(value = "reportByTaskBatch")
    public ApiResult reportByTaskBatch(@RequestParam(value = "taskid[]") String[] taskid, Boolean ifSelectAll) {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = reportService.reportByTaskBatch(taskid, username, ifSelectAll);
        } catch (Exception e) {
            logger.error("获取任务号批量报告信息出错!" + e.getMessage());
            return error("获取报告信息出错!");
        }
        return success(result);
    }

    @GetMapping(value = "reportByPlat")
    public ApiResult reportByPlat(@RequestParam(value = "taskid[]") String[] taskid, String webtype) {
        JSONObject result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = reportService.reportByPlat(taskid, webtype, username);
        } catch (Exception e) {
            logger.error("获取任务号批量报告信息出错!" + e.getMessage());
            return error("获取报告信息出错!");
        }
        return success(result);
    }

    @GetMapping(value = "exportPosite")
    public void exportPosite(@RequestParam(value = "taskid") String taskid, HttpServletResponse response, HttpServletRequest request){
        String username = this.getCurrentUser().getUsername();
        try {
            reportService.exportPosite(taskid,username,response,request);
        } catch (CustomerException e) {
            e.printStackTrace();
        }
    }
}

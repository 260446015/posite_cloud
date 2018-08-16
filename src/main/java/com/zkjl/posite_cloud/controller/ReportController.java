package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.service.IReportService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
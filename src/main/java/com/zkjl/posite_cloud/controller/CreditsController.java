package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/10 17:18
 * 积分预警
 **/
@RestController
@RequestMapping(value = "api")
public class CreditsController extends BaseController{

    private static Logger log = LoggerFactory.getLogger(CreditsController.class);
    @Resource
    private ICreditsService creditsService;
    @GetMapping(value = "warning")
    public ApiResult creditsWarning(){
        String username = this.getCurrentUser().getUsername();
        List<JSONObject> result = null;
        try {
            result = creditsService.creditsWarining(username);
        } catch (Exception e) {
            log.error("分析积分预警失败！",e.getMessage());
            return error("分析积分预警失败！");
        }
        return success(result);
    }

}

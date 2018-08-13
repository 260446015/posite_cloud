package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/10 17:18
 * 积分预警
 **/
@RestController
@RequestMapping(value = "api")
public class CreditsController extends BaseController{

    @Resource
    private ICreditsService creditsService;
    @GetMapping(value = "warning")
    public ApiResult creditsWarning(){
        String username = this.getCurrentUser().getUsername();
        creditsService.creditsWarining(username);
        return null;
    }

}

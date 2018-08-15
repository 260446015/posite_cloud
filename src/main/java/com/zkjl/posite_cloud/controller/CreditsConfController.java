package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.service.ICreditsConfService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/14 18:04
 **/
@RestController
@RequestMapping("api")
public class CreditsConfController extends BaseController{

    private static Logger logger = LoggerFactory.getLogger(CreditsConfController.class);
    @Resource
    private ICreditsConfService creditsConfService;

    /**
     * 保存修改配置的积分规则
     * @param creditsWarn
     * @return
     */
    @PostMapping(value = "saveCreditsConf")
    @ApiOperation(value = "保存修改配置的积分规则")
    public ApiResult save(@RequestBody CreditsWarn creditsWarn){
        CreditsWarn result;
        try {
            result = creditsConfService.save(creditsWarn);
        } catch (Exception e) {
            logger.error("保存积分配置出错！",e.getMessage());
            return error("保存积分配置出错！");
        }
        return success(result);
    }

}

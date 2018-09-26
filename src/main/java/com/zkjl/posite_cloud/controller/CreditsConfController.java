package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.service.ICreditsConfService;
import com.zkjl.posite_cloud.service.ICreditsService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

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
    @Resource
    private ICreditsService creditsService;

    /**
     * 保存修改配置的积分规则
     * @param creditsWarn
     * @return
     */
    @PostMapping(value = "saveCreditsConf")
    @SystemControllerLog(description = "比重设置")
    @ApiOperation(value = "保存修改配置的积分规则")
    public ApiResult save(@RequestBody CreditsWarn creditsWarn){
        CreditsWarn result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            if(creditsWarn.getId().equals("5baad9f99881bcb65a91c2ee")){
                creditsWarn.setId(null);
            }
            creditsWarn.setUsername(username);
            result = creditsConfService.save(creditsWarn);
        } catch (Exception e) {
            logger.error("保存积分配置出错！",e.getMessage());
            return error("保存积分配置出错！");
        }
        return success(result);
    }

    /**
     * 查询配置的积分规则
     * @return
     */
    @GetMapping(value = "findCreditsConf")
    @ApiOperation(value = "查询配置的积分规则")
    public ApiResult find(){
        CreditsWarn result;
        String username;
        try {
            username = this.getCurrentUser().getUsername();
            result = creditsService.findCreditsWarnConf(username);
        } catch (Exception e) {
            logger.error("查询配置的积分规则！",e.getMessage());
            return error("查询配置的积分规则！");
        }
        return success(result);
    }

}

package com.zkjl.posite_cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.domain.dto.CreditsDTO;
import com.zkjl.posite_cloud.service.ICreditsService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    /**
     * 积分预警
     * @param creditsDTO
     * @return
     */
    @PostMapping(value = "warning")
    @ApiOperation(value = "积分预警")
    public ApiResult creditsWarning(@RequestBody CreditsDTO creditsDTO){
        String username = this.getCurrentUser().getUsername();
        PageImpl<JSONObject> result;
        try {
            creditsDTO.setUsername(username);
            result = creditsService.creditsWarining(creditsDTO);
        } catch (Exception e) {
            log.error("分析积分预警失败！",e.getMessage());
            return error("分析积分预警失败！");
        }
        return successPages(result);
    }

    /**
     * 发送邮件
     */
    @GetMapping(value = "sendEmail")
    @SystemControllerLog(description = "发送邮件")
    public void sendEmail() throws Exception {
        creditsService.sendEmail();
    }

}

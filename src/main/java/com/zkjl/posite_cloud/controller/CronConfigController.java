package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.dao.CronConfigRepository;
import com.zkjl.posite_cloud.domain.pojo.CronConfig;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/31 10:11
 **/
@RestController
@RequestMapping(value = "api")
public class CronConfigController extends BaseController{

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private CronConfigRepository cronConfigRepository;
    @GetMapping(value = "updateCron")
    public ApiResult updateCron(String cron){
        CronConfig cronConfig = mongoTemplate.findOne(new Query(Criteria.where("username").is("base")), CronConfig.class, Constans.T_CRON_CONFIG);
        String _cron = cronConfig.getCron();
        String regx = "0 0/"+cron+" * * * ?";
        cronConfig.setCron(regx);
        return success(cronConfigRepository.save(cronConfig));
    }
}

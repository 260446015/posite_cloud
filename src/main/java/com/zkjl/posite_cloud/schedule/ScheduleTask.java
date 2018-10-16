package com.zkjl.posite_cloud.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.EmailUtils;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.CronConfig;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yindawei
 * @date 2018/8/21 15:31
 **/
@Component
public class ScheduleTask implements SchedulingConfigurer {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ICreditsService creditsService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private JobInfoRepository jobInfoRepository;
    private static Logger logger = LoggerFactory.getLogger(ScheduleTask.class);

//    @Scheduled(cron = "0 0/5 * * * ?")
    public void timerToNow() {
        if(logger.isInfoEnabled()){
            logger.info("---------------定时发送邮件开始----------------");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("ifSendEmail").is(null).and("data").ne(null)).limit(1000).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> all = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
//        all = all.stream().filter(action -> action.getData() != null && action.getData().size() != 0).collect(Collectors.toList());
        Map<String, CreditsWarn> confs = new HashMap<>();
        CreditsWarn conf;
        for (JobInfo data : all) {
            data.setIfSendEmail(true);
            String username = data.getUsername();
            if (confs.get(username) == null) {
                conf = creditsService.findCreditsWarnConf(username);
                confs.put(username, conf);
            } else {
                conf = confs.get(username);
            }
            JSONArray jsonArray = data.getData();
            if (jsonArray.size() == 0) {
                continue;
            }
            int totalSorce = 0;
            for (Object obj : jsonArray) {
                JSONObject action2 = new JSONObject((Map<String, Object>) obj);
                if (conf.getLiving().getString("name").equals(action2.getString("webtype"))) {
                    totalSorce += conf.getLiving().getInteger("sorce");
                } else if (conf.getGamble().getString("name").equals(action2.getString("webtype"))) {
                    totalSorce += conf.getGamble().getInteger("sorce");
                } else if (conf.getYellow().getString("name").equals(action2.getString("webtype"))) {
                    totalSorce += conf.getYellow().getInteger("sorce");
                } else if (conf.getGame().getString("name").equals(action2.getString("webtype"))) {
                    totalSorce += conf.getGame().getInteger("sorce");
                } else if (conf.getLoans().getString("name").equals(action2.getString("webtype"))) {
                    totalSorce += conf.getLoans().getInteger("sorce");
                }
            }
            boolean warnLevel = getWarnLevel(totalSorce, conf);
            if (warnLevel) {

                JSONObject param = EmailUtils.preSendEmail(data, totalSorce);
                User user = getTargetUser(data.getUsername());
                try {
                    creditsService.sendEmail(param, user);
                } catch (Exception e) {
                    logger.error("发送邮件失败,失败数据为data:"+data);
                    logger.error("失败原因:"+e.getMessage());
                }
            }
            jobInfoRepository.save(data);
        }
    }

    private User getTargetUser(String username) {
        return userRepository.findByUsername(username);
    }

    private boolean getWarnLevel(int sorce, CreditsWarn conf) {
        if (sorce >= conf.getRedSorce()) {
            return true;
        }
        return false;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(
                //1.添加任务内容(Runnable)
                this::timerToNow,
                //2.设置执行周期(Trigger)
                triggerContext -> {
                    //2.1 从数据库获取执行周期
                    CronConfig cronConfig = mongoTemplate.findOne(new Query(Criteria.where("username").is("base")), CronConfig.class, Constans.T_CRON_CONFIG);
                    String cron = cronConfig.getCron();
                    //2.2 合法性校验.
                    if (StringUtils.isEmpty(cron)) {
                        // Omitted Code ..
                    }
                    //2.3 返回执行周期(Date)
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                }
        );
    }
}

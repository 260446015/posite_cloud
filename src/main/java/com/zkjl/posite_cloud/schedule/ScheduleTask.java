package com.zkjl.posite_cloud.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yindawei
 * @date 2018/8/21 15:31
 **/
@Component
public class ScheduleTask {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ICreditsService creditsService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private JobInfoRepository jobInfoRepository;
    private static Logger logger = LoggerFactory.getLogger(ScheduleTask.class);

    @Scheduled(cron = "0 0/5 * * * ?")
    public void timerToNow() {
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
                JSONObject param = preSendEmail(data, totalSorce);
                User user = getTargetUser(data.getUsername());
                try {
                    creditsService.sendEmail(param, user);
                    jobInfoRepository.save(data);
                } catch (Exception e) {
                    logger.error("发送邮件失败,失败数据为data:"+data);
                }
            }
        }
    }

    private User getTargetUser(String username) {
        return userRepository.findByUsername(username);
    }

    private JSONObject preSendEmail(JobInfo data, int totalSorce) {
        List<String> webname = data.getData().stream().map(action -> {
            JSONObject target = new JSONObject((Map<String, Object>) action);
            return target.getString("webname");
        }).collect(Collectors.toList());
        String plat = StringUtils.join(webname, ",");
        String date = DateUtils.getFormatString(data.getCreationTime());
        JSONObject param = new JSONObject();
        param.put("title", "重点人网络筛查平台积分预警");
        param.put("content", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;尊敬的用户您好，手机号码" + data.getMobile() + "积分达到" + totalSorce + "分，系统研判为红色预警。注册违法平台有" + plat + "。采集时间为" + date + "。");
        return param;
    }

    private boolean getWarnLevel(int sorce, CreditsWarn conf) {
        if (sorce >= conf.getRedSorce()) {
            return true;
        }
        return false;
    }
}

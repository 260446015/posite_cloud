package com.zkjl.posite_cloud.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.EmailUtils;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MarkWarnTask {

    private static Logger logger = LoggerFactory.getLogger(MarkWarnTask.class);
    @Resource
    private ICreditsService creditsService;
    @Resource
    private JobInfoRepository jobInfoRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Scheduled(cron = "0 0/1 * * * ?")
    public void doMark(){
        logger.info("处理标记人员开始！");
        Query query = new Query();
        query.addCriteria(Criteria.where("handleMark").is(null).and("data").ne(null)).limit(10000).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> all = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        Map<String, CreditsWarn> confs = new HashMap<>();
        CreditsWarn conf;
        for (JobInfo data : all) {
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

            boolean warnMark = getWarnMark(totalSorce, conf);
            data.setHandleMark(-1);

            if(warnMark){
                data.setHandleMark(0);
            }
            try {
                jobInfoRepository.save(data);
            } catch (Exception e) {
                logger.error("标记已发现人员失败:"+data);
                logger.error("失败原因:"+e.getMessage());
            }
        }
    }

    private boolean getWarnMark(int sorce, CreditsWarn conf) {
        if (sorce >= conf.getYellowSorce()) {
            return true;
        }
        return false;
    }
}

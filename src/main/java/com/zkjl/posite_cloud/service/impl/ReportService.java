package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.service.IReportService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yindawei
 * @date 2018/8/14 16:26
 **/
@Service
public class ReportService extends CreditsService implements IReportService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private CreditsRepository creditsRepository;

    @Override
    public JSONObject report(String mobile, String username) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        JSONObject result;
        List<CreditsWarn> all = creditsRepository.findByUsername(username);
        CreditsWarn conf = all.get(0);
        if (mobile.matches(regex)) {
            //生成单个报告文件
            result = generatorByMobile(mobile, conf);
        } else {
            result = generator(mobile, conf);
        }
        return result;
    }

    /**
     * 其实这里面的mobile相当于taskid
     *
     * @param mobile
     * @return
     */
    private JSONObject generator(String mobile, CreditsWarn conf) {
        Query query = new Query();
        query.addCriteria(Criteria.where("taskid").is(mobile)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> list = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        if (list.size() == 0) {
            return null;
        }
        List<JSONObject> jsonObjects = generatorList(list, conf.getUsername());
        List<JSONObject> blue = jsonObjects.stream().filter(action -> action.getInteger("sorce") > conf.getBlueSorce() && action.getInteger("sorce") <= conf.getYellowSorce()).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());
        List<JSONObject> yellow = jsonObjects.stream().filter(action -> action.getInteger("sorce") > conf.getYellowSorce() && action.getInteger("sorce") <= conf.getRedSorce()).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());
        List<JSONObject> red = jsonObjects.stream().filter(action -> action.getInteger("sorce") > conf.getRedSorce()).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());
        JSONObject result = new JSONObject();
        result.put("blue", blue);
        result.put("yellow", yellow);
        result.put("red", red);
        result.put("creationTime", DateUtils.getFormatString(list.get(0).getCreationTime()));
        getTotalKindCount(list, conf, result);
        return result;
    }

    private void getTotalKindCount(List<JobInfo> list, CreditsWarn conf, JSONObject result) {
        int gambleCount = 0;
        int loansCount = 0;
        int yellowCount = 0;
        int livingCount = 0;
        int gameCount = 0;
        for (JobInfo jobinfo : list) {
            JSONArray data = jobinfo.getData();
            for (Object obj : data) {
                JSONObject jsonObject = new JSONObject((Map<String, Object>) obj);
                if (jsonObject.getString("webtype").equals(conf.getLiving().getString("name"))) {
                    livingCount += 1;
                } else if (jsonObject.getString("webtype").equals(conf.getYellow().getString("name"))) {
                    yellowCount += 1;
                } else if (jsonObject.getString("webtype").equals(conf.getLoans().getString("name"))) {
                    loansCount += 1;
                } else if (jsonObject.getString("webtype").equals(conf.getGamble().getString("name"))) {
                    gambleCount += 1;
                } else {
                    yellowCount += 1;
                }
            }
        }
        result.put("gambleCount", gambleCount);
        result.put("loansCount", loansCount);
        result.put("yellowCount", yellowCount);
        result.put("livingCount", livingCount);
        result.put("gameCount", gameCount);
    }

    private JSONObject generatorByMobile(String mobile, CreditsWarn conf) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mobile").is(mobile).and("username").is(conf.getUsername())).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> list = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        return generatorResult(list, conf);
    }

    private JSONObject generatorResult(List<JobInfo> list, CreditsWarn conf) {
        JSONObject result = new JSONObject();
        JobInfo jobInfo = null;
        for (JobInfo element : list) {
            if (element.getData() != null) {
                jobInfo = element;
                break;
            }
        }
        if (jobInfo == null) {
            return null;
        }
        int gamble = 0;
        int loans = 0;
        int yellow = 0;
        int living = 0;
        int game = 0;
        int totalSorce = 0;
        JSONArray data = jobInfo.getData();
        if (data == null) {
            return null;
        }
        for (Object action : data) {
            JSONObject jsonObject = new JSONObject((Map<String, Object>) action);
            if (jsonObject.getString("webtype").equals(conf.getGamble().getString("name"))) {
                gamble += 1;
                totalSorce += conf.getGamble().getInteger("sorce");
            } else if (jsonObject.getString("webtype").equals(conf.getLoans().getString("name"))) {
                loans += 1;
                totalSorce += conf.getLoans().getInteger("sorce");
            } else if (jsonObject.getString("webtype").equals(conf.getYellow().getString("name"))) {
                yellow += 1;
                totalSorce += conf.getYellow().getInteger("sorce");
            } else if (jsonObject.getString("webtype").equals(conf.getLiving().getString("name"))) {
                living += 1;
                totalSorce += conf.getLiving().getInteger("sorce");
            } else {
                game += 1;
                totalSorce += conf.getGame().getInteger("sorce");
            }
        }
        result.put("gamble", gamble);
        result.put("loans", loans);
        result.put("yellow", yellow);
        result.put("living", living);
        result.put("game", game);
        result.put("totalSorce", totalSorce);
        result.put("data", jobInfo.getData());
        result.put("warnLevel", getWarnLevel(totalSorce, conf));
        result.put("creationTime", DateUtils.getFormatString(jobInfo.getCreationTime()));
        return result;
    }
}

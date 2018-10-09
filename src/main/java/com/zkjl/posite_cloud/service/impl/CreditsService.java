package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.common.util.EmailUtils;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.domain.dto.CreditsDTO;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yindawei
 * @date 2018/8/10 17:30
 **/
@Service
public class CreditsService implements ICreditsService {

    @Resource
    private CreditsRepository creditsRepository;
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public PageImpl<JSONObject> creditsWarining(CreditsDTO creditsDTO) throws Exception {
        Query query = new Query();
        Criteria criteria = Criteria.where("username").is(creditsDTO.getUsername()).and("data").exists(true);
        if (!StringUtils.isBlank(creditsDTO.getMobile())) {
            criteria.and("mobile").is(creditsDTO.getMobile());
        }
        query.addCriteria(criteria);
        List<JobInfo> list = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        if (list.size() == 0) {
            return null;
        }
        List<JSONObject> result = generatorList(list, creditsDTO.getUsername());
        System.out.println(result);
        List<JSONObject> collect = result.stream().filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(creditsDTO.getMobile())) {
                if (action.getString("mobile").equals(creditsDTO.getMobile())) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(creditsDTO.getWebname())) {
                JSONArray data = (JSONArray) action.get("data");
                for (Object obj : data) {
                    JSONObject action2 = new JSONObject((Map<String, Object>) obj);
                    if (action2.getString("webname").equals(creditsDTO.getWebname())) {
                        flag = true;
                    }
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(creditsDTO.getWebtype())) {
                JSONArray data = (JSONArray) action.get("data");
                for (Object obj : data) {
                    JSONObject action2 = new JSONObject((Map<String, Object>) obj);
                    if (action2.getString("webtype").equals(creditsDTO.getWebtype())) {
                        flag = true;
                    }
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (creditsDTO.getMinSorce() != null) {
                if (action.getInteger("sorce") >= creditsDTO.getMinSorce()) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (creditsDTO.getMaxSorce() != null) {
                if (action.getInteger("sorce") <= creditsDTO.getMaxSorce()) {
                    flag = true;
                }
            } else {
                flag = true;
            }
            return flag;
        }).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());

        return (PageImpl<JSONObject>) PageUtil.pageBeagin(collect.size(), creditsDTO.getPageNum(), creditsDTO.getPageSize(), collect);
    }

    protected String getWarnLevel(int sorce, CreditsWarn conf) {
        if (sorce >= conf.getBlueSorce() && sorce < conf.getYellowSorce()) {
            return "蓝色预警";
        } else if (sorce >= conf.getYellowSorce() && sorce < conf.getRedSorce()) {
            return "橙色预警";
        } else if (sorce >= conf.getRedSorce()) {
            return "红色预警";
        } else {
            return "正常";
        }
    }

    protected List<JSONObject> generatorList(List<JobInfo> list, String username) {
        Set<String> check = new HashSet<>();
        List<JSONObject> result = new ArrayList<>();
        List<JobInfo> collect = list.stream().filter(action -> check.add(action.getMobile()) && action.getData() != null).collect(Collectors.toList());
        CreditsWarn conf = findCreditsWarnConf(username);
        collect.forEach(action -> {
            JSONObject dataKind = new JSONObject();
            dataKind.put("gamble", new JSONArray());
            dataKind.put("loans", new JSONArray());
            dataKind.put("yellow", new JSONArray());
            dataKind.put("living", new JSONArray());
            dataKind.put("game", new JSONArray());
            JSONObject data = new JSONObject();
            int totalSorce = 0;
            JSONArray jsonArray = action.getData();
            for (Object obj : jsonArray) {
                JSONObject action2 = new JSONObject((Map<String, Object>) obj);
                if (conf.getLiving().getString("name").equals(action2.getString("webtype"))) {
                    dataKind.getJSONArray("living").add(action2);
                    totalSorce += conf.getLiving().getInteger("sorce");
                } else if (conf.getGamble().getString("name").equals(action2.getString("webtype"))) {
                    dataKind.getJSONArray("gamble").add(action2);
                    totalSorce += conf.getGamble().getInteger("sorce");
                } else if (conf.getYellow().getString("name").equals(action2.getString("webtype"))) {
                    dataKind.getJSONArray("yellow").add(action2);
                    totalSorce += conf.getYellow().getInteger("sorce");
                } else if (conf.getGame().getString("name").equals(action2.getString("webtype"))) {
                    dataKind.getJSONArray("game").add(action2);
                    totalSorce += conf.getGame().getInteger("sorce");
                } else if (conf.getLoans().getString("name").equals(action2.getString("webtype"))) {
                    dataKind.getJSONArray("loans").add(action2);
                    totalSorce += conf.getLoans().getInteger("sorce");
                }
            }
            data.put("mobile", action.getMobile());
            data.put("registCount", action.getData().size());
            data.put("sorce", totalSorce);
            data.put("data", action.getData());
            data.put("id",action.getId());
            data.put("dataKind",dataKind);
            data.put("creationTime", DateUtils.getFormatString(action.getCreationTime()));
            data.put("warnInfo", getWarnLevel(totalSorce, conf));
            result.add(data);
        });
        System.out.println(result);
        return result;
    }

    @Override
    public boolean sendEmail(JSONObject data, User user) throws Exception {
        EmailUtils.sendEamil(data, user);
        return false;
    }

    @Override
    public CreditsWarn findCreditsWarnConf(String username) {
        List<CreditsWarn> confs = creditsRepository.findByUsername(username);
        if (confs.size() == 0) {
            confs = creditsRepository.findByUsername("base");
        }
        return confs.get(0);
    }
}

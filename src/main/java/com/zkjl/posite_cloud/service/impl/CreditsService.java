package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.dto.CreditsDTO;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
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
    private JobInfoRepository jobInfoRepository;
    @Resource
    private CreditsRepository creditsRepository;

    @Override
    public PageImpl<JSONObject> creditsWarining(CreditsDTO creditsDTO) throws Exception {
        List<JobInfo> list = jobInfoRepository.findByUsername(creditsDTO.getUsername());
        List<JSONObject> result = generatorList(list);
        System.out.println(result);
        List<JSONObject> collect = result.stream().filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(creditsDTO.getMobile())) {
                if (action.getString("mobile").equals(creditsDTO.getMobile())) {
                    flag = true;
                }
            }
            return flag;
        }).filter(action -> {
            boolean flag = false;
            if (!StringUtils.isBlank(creditsDTO.getWebname())) {
                Set<JSONObject> data = (Set<JSONObject>) action.get("data");
                for (JSONObject action2 : data) {
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
                Set<JSONObject> data = (Set<JSONObject>) action.get("data");
                for (JSONObject action2 : data) {
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
        }).collect(Collectors.toList());

        return (PageImpl<JSONObject>) PageUtil.pageBeagin(creditsDTO.getPageNum(), creditsDTO.getPageSize(), collect);
    }

    private String getWarnLevel(int sorce) {
        if (sorce <= 5) {
            return "蓝色预警";
        } else if (sorce <= 8) {
            return "黄色预警";
        } else if (sorce <= 20) {
            return "橙色预警";
        } else {
            return "红色预警";
        }
    }

    protected List<JSONObject> generatorList(List<JobInfo> list) {
        Map<String, Set<JSONObject>> checkMobile = new TreeMap<>();
        list.forEach(jobInfo -> {
            String mobile = jobInfo.getMobile();
            JSONArray data = jobInfo.getData();
            data.forEach(element -> {
                JSONObject perElement = (JSONObject) JSONObject.toJSON(element);
                Boolean ifsuccess = perElement.getBoolean("success");
                if (ifsuccess) {
                    Boolean ifregister = perElement.getBoolean("register");
                    if (ifregister) {
                        Set<JSONObject> jsonObjects = checkMobile.get(mobile);
                        if (jsonObjects == null) {
                            Set<JSONObject> innerData = new HashSet<>();
                            innerData.add(perElement);
                            checkMobile.put(mobile, innerData);
                        } else {
                            jsonObjects.add(perElement);
                        }
                    }
                }
            });
        });
        List<Map.Entry<String, Set<JSONObject>>> list2 = new ArrayList<>(checkMobile.entrySet());

        Collections.sort(list2, (o1, o2) -> o2.getValue().size() - o1.getValue().size());
        List<JSONObject> result = new ArrayList<>();
        List<CreditsWarn> all = creditsRepository.findAll();
        CreditsWarn conf = all.get(0);
        list2.forEach(action -> {
            JSONObject data = new JSONObject();
            int totalSorce = 0;
            for (JSONObject action2 : action.getValue()) {
                if(conf.getLiving().getString("name").equals(action2.getString("webtype"))){
                    totalSorce += conf.getLiving().getInteger("sorce");
                }else if(conf.getGamble().getString("name").equals(action2.getString("webtype"))){
                    totalSorce += conf.getGamble().getInteger("sorce");
                }else if(conf.getYellow().getString("name").equals(action2.getString("webtype"))){
                    totalSorce += conf.getYellow().getInteger("sorce");
                }else if(conf.getGame().getString("name").equals(action2.getString("webtype"))){
                    totalSorce += conf.getGame().getInteger("sorce");
                }else if(conf.getLoans().getString("name").equals(action2.getString("webtype"))){
                    totalSorce += conf.getLoans().getInteger("sorce");
                }
            }
            data.put("mobile", action.getKey());
            data.put("registCount", action.getValue().size());
            data.put("sorce", totalSorce);
            data.put("data", action.getValue());
            result.add(data);
        });
        System.out.println(result);
        return result;
    }
}

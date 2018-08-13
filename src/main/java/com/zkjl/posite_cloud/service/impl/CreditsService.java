package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.service.ICreditsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author yindawei
 * @date 2018/8/10 17:30
 **/
@Service
public class CreditsService implements ICreditsService {

    @Resource
    private JobInfoRepository jobInfoRepository;
    @Override
    public JSONObject creditsWarining(String username) {
        List<JobInfo> list = jobInfoRepository.findByUsername(username);
        Map<String, Set<JSONObject>> checkMobile = new TreeMap<>();
        list.forEach(jobInfo -> {
            String mobile = jobInfo.getMobile();
            JSONArray data = jobInfo.getData();
            data.forEach(element ->{
                JSONObject perElement = (JSONObject) JSONObject.toJSON(element);
                Boolean ifsuccess = perElement.getBoolean("success");
                if(ifsuccess){
                    Boolean ifregister = perElement.getBoolean("register");
                    if(ifregister){
                        Set<JSONObject> jsonObjects = checkMobile.get(mobile);
                        if(jsonObjects == null){
                            Set<JSONObject> innerData = new HashSet<>();
                            innerData.add(perElement);
                            checkMobile.put(mobile,innerData);
                        }else{
                            jsonObjects.add(perElement);
                        }
                    }
                }
            });
        });
        List<Map.Entry<String, Set<JSONObject>>> list2 = new ArrayList<>(checkMobile.entrySet());

        Collections.sort(list2, (o1, o2) -> o2.getValue().size() - o1.getValue().size());

        System.out.println(list2);
        return null;
    }
}

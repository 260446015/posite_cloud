package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.MD5Util;
import com.zkjl.posite_cloud.common.util.MD5Utils;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.common.util.RequestUtils;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.enums.WeightEnum;
import com.zkjl.posite_cloud.service.IApiService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yindawei
 * @date 2018/8/9 11:20
 **/
@Service
public class ApiService implements IApiService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JobInfoRepository jobInfoRepository;
    @Resource
    private MongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Override
    public JobDTO createJob(JobDTO jobDTO) {
        String taskId = getTaskId(jobDTO);
        Query query = new Query();
        query.addCriteria(Criteria.where("taskid").is(taskId)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        JobInfo one = mongoTemplate.findOne(query, JobInfo.class, Constans.T_MOBILEDATAS);
        if (one != null) {
            BeanUtils.copyProperties(one, jobDTO);
            jobDTO.setLevel(getLevel(jobDTO));
            return jobDTO;
        }
        List<JobInfo> preSaveDatas = new ArrayList<>();
        jobDTO.getDatas().forEach(mobile -> {
            JobInfo jobInfo = new JobInfo();
            jobInfo.setTaskid(taskId);
            jobInfo.setUsername(jobDTO.getUsername());
            jobInfo.setCreationTime(Calendar.getInstance().getTime());
            jobInfo.setMobile(mobile);
            preSaveDatas.add(jobInfo);
        });
        jobInfoRepository.saveAll(preSaveDatas);
        Integer level = getLevel(jobDTO);
        jobDTO.setLevel(level);
        jobDTO.setTaskid(taskId);
        createRedisJob(jobDTO);
        return jobDTO;
    }

    private String getTaskId(JobDTO jobBean) {
        return MD5Util.encrypt(jobBean.getUsername() + StringUtils.join(jobBean.getDatas(), ","));
    }

    private void createRedisJob(JobDTO jobDTO) {
        String redisId = jobDTO.getUsername() + "_" + jobDTO.getTaskid() + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
        log.info("当前生成的jobinfo信息:" + jobDTO);
        log.info("当前生成的redisId:" + redisId);
        ListOperations listOperations = stringRedisTemplate.opsForList();
        jobDTO.getDatas().forEach(mobile -> listOperations.rightPush(redisId, mobile));
    }

    @Override
    public Boolean updateJob(JobDTO jobDTO) {
        Boolean flag = false;
        try {
            String taskId = getTaskId(jobDTO);
            String status;
            if ("start".equalsIgnoreCase(jobDTO.getStatus())) {
                status = "stop";
            } else {
                status = "start";
            }
            Integer level;
            level = getLevel(jobDTO);
            jobDTO.setLevel(level);
            String _redisId = jobDTO.getUsername() + "_" + taskId + "_" + jobDTO.getLevel() + "_" + status;
            String redisId = jobDTO.getUsername() + "_" + taskId + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
            redisTemplate.rename(_redisId, redisId);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flag;
    }

    @Override
    public JSONObject realTimeData(String username) throws Exception {
        JSONObject result = new JSONObject();
        List<JobInfo> total = jobInfoRepository.findByUsername(username);
        List<JobInfo> successData = total.stream().filter(action -> action.getData() != null).collect(Collectors.toList());
        result.put("successData", successData.size());
        result.put("totalCount", total.size());
        String percent = (successData.size() / total.size()) * 100 + "%";
        result.put("percent", percent);
        return result;
    }

    @Override
    public JSONObject developmentData(String username) throws Exception {
        List<JobInfo> total = jobInfoRepository.findByUsername(username);
        List<JobInfo> successData = total.stream().filter(action -> action.getData() != null).collect(Collectors.toList());
        Map<String, Set<JSONObject>> check = new HashMap<>();
        successData.forEach(action -> {
            Set<JSONObject> values = check.get(action.getMobile());
            if (values == null) {
                values = new HashSet<>();
            }
            for (int i = 0; i < action.getData().size(); i++) {
                JSONObject data = new JSONObject((Map<String, Object>) action.getData().get(i));
                values.add(data);
            }
            check.put(action.getMobile(), values);
        });
        int gamble = 0;
        int loans = 0;
        int yellow = 0;
        int living = 0;
        int game = 0;
        for (Map.Entry<String, Set<JSONObject>> entry : check.entrySet()) {
            Set<JSONObject> value = entry.getValue();
            for (JSONObject jsonObject : value) {
                if (jsonObject.getString("webtype").equals(WeightEnum.赌博.name())) {
                    gamble += 1;
                } else if (jsonObject.getString("webtype").equals(WeightEnum.贷款.name())) {
                    loans += 1;
                } else if (jsonObject.getString("webtype").equals(WeightEnum.涉黄.name())) {
                    yellow += 1;
                } else if (jsonObject.getString("webtype").equals(WeightEnum.直播.name())) {
                    living += 1;
                } else {
                    game += 1;
                }
            }
        }
        JSONObject result = new JSONObject();
        result.put("gamble", gamble);
        result.put("loans", loans);
        result.put("yellow", yellow);
        result.put("living", living);
        result.put("game", game);
        return result;
    }

    @Override
    public PageImpl<JobInfo> realTimeRegist(String username, Integer pageNum, Integer pageSize) {
        List<JobInfo> total = jobInfoRepository.findByUsername(username);
        List<JobInfo> successData = total.stream().filter(action -> action.getData() != null).sorted((a, b) -> b.getId().compareTo(a.getId())).collect(Collectors.toList());
        return (PageImpl<JobInfo>) PageUtil.pageBeagin(pageNum, pageSize, successData);
    }

    private Integer getLevel(JobDTO jobDTO) {
        Integer level;
        if (jobDTO.getDatas().size() == 1) {
            level = 1;
        } else if (jobDTO.getDatas().size() <= Constans.BATCH_COUNT_MIN) {
            level = 2;
        } else {
            level = 3;
        }
        return level;
    }

    @Override
    public JSONObject getSentiment() {
        long c = System.currentTimeMillis() / 1000;
        String token = MD5Utils.generateToken(c);
        String url = "http://114.55.179.202:8199/restserver/index/query/fullQuery?page_no=1&page_size=10&call_id=" + c + "&token=" + token + "&appid=**";

        JSONObject json = new JSONObject();

        //相关词
        JSONArray array = new JSONArray();

        array.add("部队");

        array.add("大人");

        array.add("杀人");

        json.put("related", array);

        //行业

        json.put("industry", 1000);

        //排序字段

        json.put("sortField", "publishTime");
        //排序方式

        json.put("sortType", "desc");

        json.put("related", array);

        //查询时间范围

        json.put("start_time", "2018 - 01 - 01 00:00:00 ");

        json.put("end_time", "2018 - 07 - 22 23:59:59 ");


        System.out.println(json.toString());
        long st = System.currentTimeMillis();
        JSONObject response = RequestUtils.sendPost(url, json.toString());
        long et = System.currentTimeMillis();

        System.out.println(et - st);

        System.out.println(response);
        return response;
    }
}

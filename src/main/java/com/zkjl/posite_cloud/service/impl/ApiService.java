package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.MD5Util;
import com.zkjl.posite_cloud.common.util.MD5Utils;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.common.util.RequestUtils;
import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;
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
    @Resource
    private CreditsRepository creditsRepository;

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
        List<String> mobiles = Arrays.asList(jobDTO.getDatas().split(","));
        mobiles.forEach(mobile -> {
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
        List<String> mobiles = Arrays.asList(jobDTO.getDatas());
        mobiles.forEach(mobile -> listOperations.rightPush(redisId, mobile));
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
        List<CreditsWarn> all = creditsRepository.findAll();
        CreditsWarn conf = all.get(0);
        for (Map.Entry<String, Set<JSONObject>> entry : check.entrySet()) {
            Set<JSONObject> value = entry.getValue();
            for (JSONObject jsonObject : value) {
                if (jsonObject.getString("webtype").equals(conf.getGamble().getString("name"))) {
                    gamble += 1;
                } else if (jsonObject.getString("webtype").equals(conf.getLoans().getString("name"))) {
                    loans += 1;
                } else if (jsonObject.getString("webtype").equals(conf.getYellow().getString("name"))) {
                    yellow += 1;
                } else if (jsonObject.getString("webtype").equals(conf.getLiving().getString("name"))) {
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
    public List<JSONObject> realTimeRegist(String username) {
        List<JobInfo> total = jobInfoRepository.findByUsername(username);
        List<JobInfo> successData = total.stream().filter(action -> action.getData() != null).sorted((a, b) -> b.getId().compareTo(a.getId())).collect(Collectors.toList());
        List<JSONObject> result = new ArrayList<>();
        A:
        for (JobInfo action : successData) {
            JSONArray data = action.getData();
            B:
            for (Object obj : data) {
                JSONObject element = new JSONObject();
                JSONObject jsonObject = new JSONObject((Map<String, Object>) obj);
                element.put("mobile", action.getMobile());
                element.put("webtype", jsonObject.getString("webtype"));
                element.put("app", jsonObject.getString("webname"));
                result.add(element);
                if (result.size() >= 20) {
                    break A;
                }
            }
        }
        return result;
    }

    private Integer getLevel(JobDTO jobDTO) {
        Integer level;
        String[] split = jobDTO.getDatas().split(",");
        if (split.length == 1) {
            level = 1;
        } else if (split.length <= Constans.BATCH_COUNT_MIN) {
            level = 2;
        } else {
            level = 3;
        }
        return level;
    }

    @Override
    public JSONObject getSentiment(SentimentDTO sentimentDTO) {
        long c = System.currentTimeMillis() / 1000;
        String token = MD5Utils.generateToken(c);
        Map<String, Object> params = new HashMap<>();
        params.put("page_no", sentimentDTO.getPageNum());
        params.put("page_size", sentimentDTO.getPageSize());
        params.put("call_id", c);
        params.put("token", token);
        params.put("appid", "**");
        String assembling = RequestUtils.assembling(params);
        JSONObject json = new JSONObject();
        //相关词
        JSONArray array = new JSONArray();
        String[] msg = sentimentDTO.getMsg();
        for (int i = 0; i < msg.length; i++) {
            array.add(msg[i]);
        }
        json.put("related", array);
        //行业
        json.put("industry", 1000);
        //排序字段
        json.put("sortField", "publishTime");
        //排序方式
        json.put("sortType", "desc");
        json.put("related", array);
        //查询时间范围
        json.put("start_time", sentimentDTO.getBeginDate());
        json.put("end_time", sentimentDTO.getEndDate());
        System.out.println(json.toString());
        long st = System.currentTimeMillis();
        JSONObject response = RequestUtils.sendPost(Constans.SENTIMENT_URL + "?" + assembling, json.toString());
        long et = System.currentTimeMillis();
        System.out.println(et - st);
        System.out.println(response);
        return response;
    }

    @Override
    public List<JobinfoVO> listJob(String username) {
        List<JobinfoVO> result = new ArrayList<>();
        List<JobInfo> byUsername = jobInfoRepository.findByUsername(username);
        Set<String> check = new HashSet<>();
        List<JobInfo> collect = byUsername.stream().filter(action -> check.add(action.getTaskid())).collect(Collectors.toList());
        Set<String> keys = stringRedisTemplate.keys(username + "*");
        collect.forEach(action -> {
            JobinfoVO vo = new JobinfoVO();
            vo.setTaskId(action.getTaskid());
            vo.setCreationTime(action.getCreationTime());
            vo.setIfFinish(true);
            for (String redisId : keys) {
                if (redisId.contains(action.getTaskid())) {
                    vo.setIfFinish(false);
                }
            }
            result.add(vo);
        });
        return result;
//        GroupBy groupBy = GroupBy.key("taskid").initialDocument("{count:0}").reduceFunction("function(doc, out){out.count++}")
//                .finalizeFunction("function(out){return out;}");
//        GroupByResults<JobInfo> res = mongoTemplate.group(Constans.T_MOBILEDATAS, groupBy, JobInfo.class);
////        DBObject obj = res.getRawResults();
////        BasicDBList dbList = (BasicDBList) obj.get("retval");
//        List<Document> retval = (List) res.getRawResults().get("retval");
//        retval.forEach(action ->{
//            String taskid = action.getString("taskid");
//            result.add(taskid);
//        });
    }

    @Override
    public JSONObject searchByTaskid(String taskId, Integer pageNum, Integer pageSize, String msg) {
        JSONObject result = new JSONObject();
        List<JobInfo> byTaskId = jobInfoRepository.findByTaskid(taskId);
        if (!StringUtils.isBlank(msg)) {
            byTaskId = byTaskId.stream().filter(action -> action.getMobile().equals(msg)).collect(Collectors.toList());
        }
        PageImpl<JobInfo> page = (PageImpl<JobInfo>) PageUtil.pageBeagin(byTaskId.size(), pageNum, pageSize, byTaskId);

        result.put("data", successPages(page));
        return result;
    }

    @Override
    public JSONObject searchByTaskidPlan(String taskId) {
        JSONObject result = new JSONObject();
        List<JobInfo> byTaskId = jobInfoRepository.findByTaskid(taskId);
        int successCount = 0;
        for (JobInfo jobInfo : byTaskId) {
            if (jobInfo.getData() != null) {
                successCount += 1;
            }
        }
        result.put("successCount", successCount);
        result.put("totalCount", byTaskId.size());
        return result;
    }

    private JSONObject successPages(PageImpl<?> data) {
        JSONObject result = new JSONObject();
        result.put("dataList", data.getContent());
        result.put("totalNumber", data.getTotalElements());
        result.put("totalPage", data.getTotalPages());
        result.put("pageNumber", data.getNumber());
        return result;
    }
}

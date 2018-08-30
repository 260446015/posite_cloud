package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.*;
import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.dao.RedistaskRepository;
import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.pojo.Redistask;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;
import com.zkjl.posite_cloud.service.IApiService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
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
    private RedistaskRepository redistaskRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private CreditsRepository creditsRepository;
    @Resource
    private CreditsService creditsService;
    @Resource
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Override
    public JobDTO createJob(JobDTO jobDTO) {
        checkMobile(jobDTO);
        String taskId = getTaskId(jobDTO);
        List<JobInfo> preSaveDatas = new ArrayList<>();
        List<String> mobiles = Arrays.asList(jobDTO.getDatas().split(","));
        Set<String> check = new HashSet<>();
        for (String mobile : mobiles) {
            check.add(mobile);
            JobInfo jobInfo = new JobInfo();
            jobInfo.setTaskid(taskId);
            jobInfo.setUsername(jobDTO.getUsername());
            jobInfo.setCreationTime(Calendar.getInstance().getTime());
            jobInfo.setMobile(mobile);
            preSaveDatas.add(jobInfo);
        }
        jobInfoRepository.saveAll(preSaveDatas);
        redistaskRepository.save(new Redistask(jobDTO.getUsername(), taskId, false, jobDTO.getTaskname()));
        Integer level = getLevel(jobDTO);
        jobDTO.setLevel(level);
        jobDTO.setTaskid(taskId);
        createRedisJob(jobDTO);
        return jobDTO;
    }

    private void checkMobile(JobDTO jobDTO) {
        String datas = jobDTO.getDatas();
        String[] split = datas.split(",");
        log.info("校验数据之前的数量:" + split.length);
        Set<String> check = new HashSet<>();
        for (String aSplit : split) {
            if (RegUtil.checkMobile(aSplit)) {
                check.add(aSplit);
            }
        }
        String join = StringUtils.join(check, ",");
        String[] split1 = join.split(",");
        log.info("校验数据之后的数量:" + split1.length);
        jobDTO.setDatas(join);
    }

    private String getTaskId(JobDTO jobBean) {
        return MD5Util.encrypt(jobBean.getUsername() + jobBean.getDatas() + System.currentTimeMillis());
    }

    private void createRedisJob(JobDTO jobDTO) {
        String redisId = jobDTO.getUsername() + "_" + jobDTO.getTaskid() + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
        log.info("当前生成的jobinfo信息:" + jobDTO);
        log.info("当前生成的redisId:" + redisId);
        ListOperations listOperations = stringRedisTemplate.opsForList();
        List<String> mobiles = Arrays.asList(jobDTO.getDatas().split(","));
        mobiles.forEach(mobile -> listOperations.rightPush(redisId, mobile));
    }

    @Override
    public Boolean updateJob(JobDTO jobDTO) {
        Boolean flag = false;
        try {
            String taskId = jobDTO.getTaskid();
            String status;
            if ("start".equalsIgnoreCase(jobDTO.getStatus())) {
                status = "stop";
            } else {
                status = "start";
            }
            Integer level;
            List<JobInfo> byTaskid = jobInfoRepository.findByTaskid(taskId);
            List<String> mobiles = byTaskid.stream().map(JobInfo::getMobile).collect(Collectors.toList());
            level = getLevel(byTaskid.size());
            jobDTO.setLevel(level);
            String _redisId = jobDTO.getUsername() + "_" + taskId + "_" + jobDTO.getLevel() + "_" + status;
            String redisId = jobDTO.getUsername() + "_" + taskId + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
            try {
                stringRedisTemplate.rename(_redisId, redisId);
            } catch (Exception e) {
                Redistask oldRedis = mongoTemplate.findOne(new Query(Criteria.where("taskid").is(taskId)).with(Sort.by(Sort.Direction.DESC, "_version")), Redistask.class, Constans.T_REDISTASK);
                oldRedis.setDatas(byTaskid);
                redistaskRepository.save(oldRedis);
                byTaskid.forEach(action -> action.setData(null));
                jobInfoRepository.saveAll(byTaskid);
                Redistask newRedis = new Redistask(oldRedis.getUsername(), taskId, false, oldRedis.getTaskname());
                newRedis.setCreationTime(Calendar.getInstance().getTime());
                newRedis.setIfFinish(false);
                newRedis.setTaskid(taskId);
                newRedis.setTaskname(oldRedis.getTaskname());
                newRedis.setUsername(oldRedis.getUsername());
                int version = oldRedis.get_version() + 1;
                newRedis.set_version(version);
                redistaskRepository.save(newRedis);
                ListOperations<String, String> stringStringListOperations = stringRedisTemplate.opsForList();
                stringStringListOperations.trim(redisId, 1, 0);
                mobiles.forEach(mobile -> stringStringListOperations.rightPush(redisId, mobile));
            }
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
        if (total.size() == 0) {
            result.put("successData", 0);
            result.put("totalCount", 0);
            result.put("percent", "100%");
            return result;
        }
        List<JobInfo> successData = total.stream().filter(action -> action.getData() != null).collect(Collectors.toList());
        result.put("successData", successData.size());
        result.put("totalCount", total.size());
        BigDecimal successCount = new BigDecimal(successData.size());
        BigDecimal totalCount = new BigDecimal(total.size());
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(4);
        String format = percent.format(successCount.divide(totalCount, 4, RoundingMode.HALF_UP));
        result.put("percent", format);
        return result;
    }

    @Override
    public JSONObject developmentData(String username) throws Exception {
        List<JobInfo> total = jobInfoRepository.findByUsername(username);
        List<JobInfo> successData = total.stream().filter(action -> action.getData() != null && action.getData().size() != 0).collect(Collectors.toList());
        if (successData.size() == 0) {
            JSONObject result = new JSONObject();
            result.put("gamble", 0);
            result.put("loans", 0);
            result.put("yellow", 0);
            result.put("living", 0);
            result.put("game", 0);
            return result;
        }
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
        CreditsWarn conf = creditsService.findCreditsWarnConf(username);
        Set<String> checkPerson = new HashSet<>();
        for (Map.Entry<String, Set<JSONObject>> entry : check.entrySet()) {
            Set<JSONObject> value = entry.getValue();
            checkPerson.clear();
            for (JSONObject jsonObject : value) {
                if (jsonObject.getString("webtype").equals(conf.getGamble().getString("name"))) {
                    if (checkPerson.add("gamble")) {
                        gamble += 1;
                    }
                } else if (jsonObject.getString("webtype").equals(conf.getLoans().getString("name"))) {
                    if (checkPerson.add("loans")) {
                        loans += 1;
                    }
                } else if (jsonObject.getString("webtype").equals(conf.getYellow().getString("name"))) {
                    if (checkPerson.add("yellow")) {
                        yellow += 1;
                    }
                } else if (jsonObject.getString("webtype").equals(conf.getLiving().getString("name"))) {
                    if (checkPerson.add("living")) {
                        living += 1;
                    }
                } else {
                    if (checkPerson.add("game")) {
                        game += 1;
                    }
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
        List<JSONObject> result = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username).and("data").exists(true)).limit(1000).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> list = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        A:
        for (JobInfo action : list) {
            JSONArray data = action.getData();
            B:
            for (Object obj : data) {
                JSONObject element = new JSONObject();
                JSONObject jsonObject = new JSONObject((Map<String, Object>) obj);
                element.put("mobile", action.getMobile());
                element.put("webtype", jsonObject.getString("webtype"));
                element.put("app", jsonObject.getString("webname"));
                element.put("webnames", data);
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

    private Integer getLevel(int level) {
        if (level == 1) {
            level = 1;
        } else if (level <= Constans.BATCH_COUNT_MIN) {
            level = 2;
        } else {
            level = 3;
        }
        return level;
    }

    @Override
    public JSONObject getSentiment(SentimentDTO sentimentDTO, String userSentiment) {
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
        Set<String> check = new HashSet<>();
        check.add("赌博");
        check.add("贷款");
        check.add("色情");
        check.add("黄色");
        check.add("主播");
        check.add("直播");
        check.add("游戏");
        /*String area = "";
        try {
            String myIP = IpUtil.getMyIP();
            area = IpUtil.baiduGetCityCode(myIP);
            check.add(area);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if (!StringUtils.isBlank(userSentiment)) {
            String[] split = userSentiment.split(",");
            Collections.addAll(check, split);
        }
        array.addAll(check);
        json.put("industry", 1000);
        //排序字段
        json.put("sortField", "publishTime");
        //排序方式
        json.put("sortType", "desc");
        //行业
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
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("username").is(username)),
                Aggregation.sort(Sort.Direction.DESC,"creationTime"),
                Aggregation.group("taskid").last("_version").as("_version").first("taskid").as("taskid").first("taskname").as("taskname").first("creationTime").as("creationTime").first("username").as("username")
        );

        AggregationResults<Redistask> outputType = mongoTemplate.aggregate(agg, Constans.T_REDISTASK, Redistask.class);
        List data = (List) outputType.getRawResults().get("result");
        List<JobinfoVO> result = new ArrayList<>();
        Set<String> keys = stringRedisTemplate.keys(username + "*");
        data.forEach(action -> {
            JSONObject element = new JSONObject((Map<String, Object>) action);
            JobinfoVO vo = new JobinfoVO();
            if (StringUtils.isBlank(element.getString("taskid"))) {
                vo.setTaskId(element.getString("_id"));
            } else {
                vo.setTaskId(element.getString("taskid"));
            }
            vo.setCreationTime(element.getDate("creationTime"));
            vo.setIfFinish(true);
            vo.setTaskname(element.getString("taskname"));
            vo.setReportStatus(false);
            if(element.getInteger("_version") > 0){
                vo.setReportStatus(true);
            }
            for (String redisId : keys) {
                if (StringUtils.isBlank(element.getString("taskid"))) {
                    continue;
                }
                if (redisId.contains(element.getString("taskid"))) {
                    vo.setIfFinish(false);
                }
            }
            result.add(vo);
        });
        return result;
    }

    @Override
    public JSONObject searchByTaskid(String taskId, Integer pageNum, Integer pageSize, String msg) {
        JSONObject result = new JSONObject();
        Query query = new Query();
        query.addCriteria(Criteria.where("taskid").is(taskId).and("data").exists(true));
        List<JobInfo> byTaskId = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        byTaskId = byTaskId.stream().filter(action -> action.getData() != null && action.getData().size() != 0).collect(Collectors.toList());
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
        if (byTaskId.size() == 0) {
            result.put("percent", "100%");
            result.put("successCount", 0);
            result.put("totalCount", 0);
            return result;
        }
        int success = 0;
        for (JobInfo jobInfo : byTaskId) {
            if (jobInfo.getData() != null) {
                success += 1;
            }
        }
        BigDecimal successCount = new BigDecimal(success);
        BigDecimal totalCount = new BigDecimal(byTaskId.size());
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(4);
        String format = percent.format(successCount.divide(totalCount, 4, RoundingMode.HALF_UP));
        result.put("percent", format);
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

    @Override
    public boolean deleteBatch(String[] ids, String username) {
        boolean flag = false;
        try {
            for (int i = 0; i < ids.length; i++) {
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(ids[i])),Constans.T_REDISTASK);
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(ids[i])),Constans.T_MOBILEDATAS);
                try {
                    Set keys = redisTemplate.keys(username + "_" + ids[i] + "_*");
                    redisTemplate.delete(keys);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            flag = true;
        } catch (Exception e) {
            log.error("根据任务id删除任务出错!", e.getMessage());
        }
        return flag;
    }
}

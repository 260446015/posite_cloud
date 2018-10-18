package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.*;
import com.zkjl.posite_cloud.dao.*;
import com.zkjl.posite_cloud.domain.dto.DeleteJobDTO;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.domain.pojo.*;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IApiService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
    @Resource
    private AssignTaskRepository assignTaskRepository;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Override
    @CacheEvict(key = "#p0.username", value = "redisJob")
    public JobDTO createJob(JobDTO jobDTO) throws CustomerException {
        checkMobile(jobDTO);
        String taskId = getTaskId(jobDTO);
        List<JobInfo> preSaveDatas = new ArrayList<>();
        List<String> mobiles = Arrays.asList(jobDTO.getDatas().split(","));
        checkAllowImport(jobDTO, mobiles);
        for (String mobile : mobiles) {
            JobInfo jobInfo = new JobInfo();
            jobInfo.setTaskid(taskId);
            jobInfo.setUsername(jobDTO.getUsername());
            jobInfo.setCreationTime(Calendar.getInstance().getTime());
            jobInfo.setMobile(mobile.split("`")[0].trim());
            try {
                jobInfo.setMobileUser(mobile.split("`")[1].trim());
            } catch (Exception e) {
                jobInfo.setMobileUser("");
            }
            preSaveDatas.add(jobInfo);
        }
        jobInfoRepository.saveAll(preSaveDatas);
        redistaskRepository.save(new Redistask(jobDTO.getUsername(), taskId, false, jobDTO.getTaskname(), preSaveDatas.size()));
        Integer level = getLevel(jobDTO);
        jobDTO.setLevel(level);
        jobDTO.setTaskid(taskId);
        createRedisJob(jobDTO);
        return jobDTO;
    }

    private void checkAllowImport(JobDTO jobDTO, List<String> mobiles) throws CustomerException {
        User byUsername = userRepository.findByUsername(jobDTO.getUsername());
        int searchCount = byUsername.getSearchCount();
        int totalSerachCount = byUsername.getTotalSerachCount();
        if ((totalSerachCount - searchCount) <= 0 || (totalSerachCount - searchCount) < mobiles.size()) {
            throw new CustomerException("查询可用数量不足，请联系管理员进行修改");
        }
        searchCount += mobiles.size();
        log.info("当前用户:" + jobDTO.getUsername() + ",剩余数量:" + searchCount);
        byUsername.setSearchCount(searchCount);
        userRepository.save(byUsername);
    }

    private void checkMobile(JobDTO jobDTO) throws CustomerException {
        String datas = jobDTO.getDatas();
        String[] split = datas.split(",");
        log.info("校验数据之前的数量:" + split.length);
        Set<String> check = new HashSet<>();
        for (String aSplit : split) {
            check.add(aSplit);
        }
        if (check.size() == 0) {
            throw new CustomerException("传入的数据不能为空，请确认数据传入无误");
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
        Set<String> datas = new HashSet<>();
        for (int i = 0; i < mobiles.size(); i++) {
            datas.add(mobiles.get(i).split("`")[0].trim());
        }
        listOperations.rightPushAll(redisId, datas);
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
                Redistask newRedis = new Redistask(oldRedis.getUsername(), taskId, false, oldRedis.getTaskname(), byTaskid.size());
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
        User byUsername = userRepository.findByUsername(username);
        result.put("searchCount", (byUsername.getTotalSerachCount() - byUsername.getSearchCount()));
        result.put("totalSerachCount", byUsername.getTotalSerachCount());
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
        Set keys = redisTemplate.keys(username + "*");
        if (keys.size() == 0) {
            result.put("percent", "100%");
        }
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
        String[] msg = sentimentDTO.getMsg();
        for (String input : msg) {
            check.add(input);
        }
        if (!StringUtils.isBlank(userSentiment)) {
            String[] split = userSentiment.split(",");
            Collections.addAll(check, split);
        }
        array.addAll(check);
        if (log.isInfoEnabled()) {
            log.info("查询舆情开始,参数为:userSentiment:" + userSentiment + ",舆情参数:" + check + ",sentimentDTO:" + sentimentDTO);
        }
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
        long st = System.currentTimeMillis();
        JSONObject response = RequestUtils.sendPost(Constans.SENTIMENT_URL + "?" + assembling, json.toString());
        long et = System.currentTimeMillis();
        System.out.println(et - st);
        System.out.println(response);
        return response;
    }

    @Override
    @Cacheable(key = "#p0", value = "redisJob")
    public JSONObject listAllJob(String username) {
        JSONObject result = new JSONObject();
//        List<JobinfoVO> jsonObject = listJob2(username);
//        result.put("job", jsonObject);
        User byUsername = userRepository.findByUsername(username);
        if (byUsername.getJobLevel().equals("admin")) {

            List<JobinfoVO> adminData = listJob2(username);
            adminData.addAll(listAssignJob(byUsername.getId(), username));
            result.put("job", adminData);
            result.put("mark", 1);
            result.put("user", null);
            result.put("username", username);
            result.put("userid", byUsername.getId());
            List<User> secondUser = userRepository.findByCreator(username);
            if (secondUser.size() != 0) {
                JSONArray secondArr = new JSONArray();
                for (User aSecondUser : secondUser) {
                    JSONObject element = new JSONObject();
                    String elementUsername = aSecondUser.getUsername();
                    List<JobinfoVO> jobinfoVOS = listJob2(elementUsername);
                    jobinfoVOS.addAll(listAssignJob(aSecondUser.getId(), elementUsername));
                    element.put("job", jobinfoVOS);
                    element.put("user", null);
                    element.put("mark", 2);
                    element.put("username", elementUsername);
                    element.put("userid", aSecondUser.getId());
                    List<User> thirdUser = userRepository.findByCreator(elementUsername);
                    if (thirdUser.size() != 0) {
                        JSONArray thirdArr = new JSONArray();
                        for (int i = 0; i < thirdUser.size(); i++) {
                            JSONObject elementThird = new JSONObject();
                            String elementUsernameThird = thirdUser.get(i).getUsername();
                            List<JobinfoVO> jobinfoVOS1 = listJob2(elementUsernameThird);
                            jobinfoVOS1.addAll(listAssignJob(thirdUser.get(i).getId(), elementUsernameThird));
                            elementThird.put("job", jobinfoVOS1);
                            elementThird.put("mark", 3);
                            elementThird.put("username", elementUsernameThird);
                            elementThird.put("userid", thirdUser.get(i).getId());
                            thirdArr.add(elementThird);
                        }
                        element.put("user", thirdArr);

                    }
                    secondArr.add(element);
                }
                result.put("user", secondArr);

            }
        } else if (byUsername.getJobLevel().equals("group")) {
            List<JobinfoVO> groupData = listJob2(username);
            groupData.addAll(listAssignJob(byUsername.getId(), username));
            result.put("job", groupData);
            result.put("mark", 2);
            result.put("user", null);
            result.put("username", username);
            result.put("userid", byUsername.getId());
            List<User> secondUser = userRepository.findByCreator(username);
            if (secondUser.size() != 0) {
                JSONArray sendArr = new JSONArray();
                for (int i = 0; i < secondUser.size(); i++) {
                    JSONObject element = new JSONObject();
                    String elementUsername = secondUser.get(i).getUsername();
                    List<JobinfoVO> jobinfoVOS = listJob2(elementUsername);
                    jobinfoVOS.addAll(listAssignJob(secondUser.get(i).getId(), elementUsername));
                    element.put("job", jobinfoVOS);
                    element.put("mark", 3);
                    element.put("username", elementUsername);
                    element.put("userid", secondUser.get(i).getId());
                    sendArr.add(element);
                }
                result.put("user", sendArr);

            }
        } else {
            List<JobinfoVO> normalData = listJob2(username);
            normalData.addAll(listAssignJob(byUsername.getId(), username));
            result.put("job", normalData);
            result.put("username", username);
            result.put("username", byUsername.getId());
            result.put("mark", 3);
        }

        return result;
    }

    @Override
    public JSONObject listJob(String username) {
        JSONObject resultData = new JSONObject();
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("username").is(username)),
                Aggregation.sort(Sort.Direction.ASC, "creationTime"),
                Aggregation.group("taskid").last("_version").as("_version").first("taskid").as("taskid").first("taskname").as("taskname").last("creationTime").as("creationTime").first("username").as("username")
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
            vo.setCreationTime(DateUtils.getFormatString(element.getDate("creationTime")));
            vo.setIfFinish(true);
            vo.setTaskname(element.getString("taskname"));
            vo.setReportStatus(false);
            if (element.getInteger("_version") > 0) {
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
        result.sort(Comparator.comparing(JobinfoVO::getCreationTime));
        JSONObject nextData = new JSONObject();
        nextData.put("job", result);
        List<User> byCreator = userRepository.findByCreator(username);
        if (byCreator.size() != 0) {
            byCreator.forEach(action -> {
                JSONObject next = new JSONObject();
                next.put(action.getUsername(), null);

            });
        }
        resultData.put(username, result);
        return resultData;
    }

    private List<JobinfoVO> listAssignJob(String userid, String username) {
        List<AssignTask> assignTasks = assignTaskRepository.findByUserid(userid);
        List<String> taskids = assignTasks.stream().map(AssignTask::getTaskid).collect(Collectors.toList());
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskid").in(taskids)),
                Aggregation.sort(Sort.Direction.ASC, "creationTime"),
                Aggregation.group("taskid").last("_version").as("_version").first("taskid").as("taskid").first("taskname").as("taskname").last("creationTime").as("creationTime").first("username").as("username").last("uploadSize").as("uploadSize")
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
            vo.setCreationTime(DateUtils.getFormatString(element.getDate("creationTime")));
            vo.setIfFinish(true);
            vo.setUploadSize(element.getInteger("uploadSize"));
            vo.setTaskname(element.getString("taskname"));
            vo.setReportStatus(false);
            if (element.getInteger("_version") > 0) {
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
        result.sort(Comparator.comparing(JobinfoVO::getCreationTime));
        JSONObject nextData = new JSONObject();
        nextData.put("job", result);
        List<User> byCreator = userRepository.findByCreator(username);
        if (byCreator.size() != 0) {
            byCreator.forEach(action -> {
                JSONObject next = new JSONObject();
                next.put(action.getUsername(), null);

            });
        }
        return result;

    }

    private List<JobinfoVO> listJob2(String username) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("username").is(username)),
                Aggregation.sort(Sort.Direction.ASC, "creationTime"),
                Aggregation.group("taskid").last("_version").as("_version").first("taskid").as("taskid").first("taskname").as("taskname").last("creationTime").as("creationTime").first("username").as("username").last("uploadSize").as("uploadSize")
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
            vo.setCreationTime(DateUtils.getFormatString(element.getDate("creationTime")));
            vo.setIfFinish(true);
            vo.setTaskname(element.getString("taskname"));
            vo.setReportStatus(false);
            vo.setUploadSize(element.getInteger("uploadSize"));
            if (element.getInteger("_version") > 0) {
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
        result.sort(Comparator.comparing(JobinfoVO::getCreationTime));
        JSONObject nextData = new JSONObject();
        nextData.put("job", result);
        List<User> byCreator = userRepository.findByCreator(username);
        if (byCreator.size() != 0) {
            byCreator.forEach(action -> {
                JSONObject next = new JSONObject();
                next.put(action.getUsername(), null);

            });
        }
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
        Set keys = redisTemplate.keys("*" + taskId + "*");
        if (keys.size() == 0) {
            result.put("percent", "100%");
        }
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
    @CacheEvict(value = "redisJob", key = "#p1")
    public boolean deleteBatch(String[] ids, String userid, String username) {
        boolean flag = false;
        try {
            for (int i = 0; i < ids.length; i++) {
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(ids[i])), Constans.T_REDISTASK);
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(ids[i])), Constans.T_MOBILEDATAS);
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

    @Override
    public boolean deleteBatch(List<DeleteJobDTO> deletes, String username) {
        for (int i = 0; i < deletes.size(); i++) {
            DeleteJobDTO action = deletes.get(i);
            String taskid = action.getTaskid();
            String userid = action.getUserid();
            if (StringUtils.isBlank(userid)) {
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(taskid)), Constans.T_REDISTASK);
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(taskid)), Constans.T_MOBILEDATAS);
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(taskid)), Constans.T_ASSIGN_TASK);
                try {
                    Set keys = redisTemplate.keys(username + "_" + taskid + "_*");
                    redisTemplate.delete(keys);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mongoTemplate.remove(new Query(Criteria.where("taskid").is(taskid).and("userid").is(userid)), Constans.T_ASSIGN_TASK);
            }
        }
        return false;
    }

    @Override
    public boolean taskAssignment(String[] taskid, String[] userid) {
        List<AssignTask> saveData = new ArrayList<>();
        for (int i = 0; i < userid.length; i++) {
            for (int j = 0; j < taskid.length; j++) {
                AssignTask assignTask = new AssignTask();
                String _id = MD5Util.encrypt(userid[i] + taskid[j]);
                assignTask.setId(_id);
                assignTask.setUserid(userid[i]);
                assignTask.setTaskid(taskid[j]);
                assignTask.setCreateTime(Calendar.getInstance().getTime());
                saveData.add(assignTask);
            }
        }
        try {
            assignTaskRepository.saveAll(saveData);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean updatePersonMark(Integer handleMark, String id) throws CustomerException {
        Optional<JobInfo> byId = jobInfoRepository.findById(id);
        JobInfo jobInfo = byId.orElse(null);
        if (jobInfo == null) {
            throw new CustomerException("未找到相应的重点人员!");
        }
        jobInfo.setHandleMark(handleMark);
        try {
            jobInfoRepository.save(jobInfo);
        } catch (Exception e) {
            log.error("标记重点人员失败,参数:" + handleMark + "," + id);
            return false;
        }
        return true;
    }
}

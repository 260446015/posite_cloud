package com.zkjl.posite_cloud.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.dao.UpdateTaskRepository;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.pojo.Redistask;
import com.zkjl.posite_cloud.domain.pojo.UpdateTask;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;
import com.zkjl.posite_cloud.domain.vo.RedistaskVO;
import com.zkjl.posite_cloud.service.IReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
    @Resource
    private UpdateTaskRepository updateTaskRepository;
    @Resource
    private CreditsService creditsService;
    @Resource
    private JobInfoRepository jobInfoRepository;
    @Resource
    private ApiService apiService;

    @Override
    public JSONObject report(String mobile, String username) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        JSONObject result;
        CreditsWarn conf = findCreditsWarnConf(username);
        if (mobile.matches(regex)) {
            //生成单个报告文件
            result = generatorByMobile(mobile, conf, username);
        } else {
            result = generator(new String[]{mobile}, conf, username);
        }
        return result;
    }

    /**
     * 其实这里面的mobile相当于taskid
     *
     * @param mobile
     * @return
     */
    private JSONObject generator(String[] mobile, CreditsWarn conf, String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("taskid").in(mobile)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> list = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        if (list.size() == 0) {
            return null;
        }
        List<JSONObject> jsonObjects = generatorList(list, username);
        JSONObject result = getWarnKindObject(conf, jsonObjects);
        result.put("creationTime", DateUtils.getFormatString(list.get(0).getCreationTime()));
        getTotalKindCount(list, conf, result);
        return result;
    }

    private JSONObject getWarnKindObject(CreditsWarn conf, List<JSONObject> jsonObjects) {
        List<JSONObject> blue = jsonObjects.stream().filter(action -> action.getInteger("sorce") > conf.getBlueSorce() && action.getInteger("sorce") <= conf.getYellowSorce()).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());
        List<JSONObject> yellow = jsonObjects.stream().filter(action -> action.getInteger("sorce") > conf.getYellowSorce() && action.getInteger("sorce") <= conf.getRedSorce()).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());
        List<JSONObject> red = jsonObjects.stream().filter(action -> action.getInteger("sorce") > conf.getRedSorce()).sorted((a, b) -> b.getInteger("sorce").compareTo(a.getInteger("sorce"))).collect(Collectors.toList());
        JSONObject result = new JSONObject();
        result.put("blue", blue);
        result.put("yellow", yellow);
        result.put("red", red);
        return result;
    }

    private void getTotalKindCount(List<JobInfo> list, CreditsWarn conf, JSONObject result) {
        int gamble = 0;
        int loans = 0;
        int yellow = 0;
        int living = 0;
        int game = 0;
        Set<String> checkPerson = new HashSet<>();
        for (JobInfo jobInfo : list) {
            JSONArray value = jobInfo.getData();
            checkPerson.clear();
            if (value == null) {
                continue;
            }
            for (Object obj : value) {
                JSONObject jsonObject = new JSONObject((Map<String, Object>) obj);
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
        result.put("gambleCount", gamble);
        result.put("loansCount", loans);
        result.put("yellowCount", yellow);
        result.put("livingCount", living);
        result.put("gameCount", game);
        /*int gambleCount = 0;
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
        result.put("gameCount", gameCount);*/
    }

    private JSONObject generatorByMobile(String mobile, CreditsWarn conf, String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mobile").is(mobile).and("username").is(username)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
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
        if (data == null || data.size() == 0) {
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

    @Override
    public PageImpl<RedistaskVO> findReportHistory(String taskid, Integer pageNum, Integer pageSize, String username) {
        List<Redistask> list = mongoTemplate.find(new Query(Criteria.where("taskid").is(taskid).and("datas").ne(null)).with(Sort.by(Sort.Direction.DESC, "_version")), Redistask.class, Constans.T_REDISTASK);
        if (list.size() == 0) {
            return null;
        }
        List<RedistaskVO> result = new ArrayList<>();
        CreditsWarn conf = creditsService.findCreditsWarnConf(username);
        List<JobInfo> termData = new ArrayList<>();
        list.forEach(action -> {
            int blue = 0;
            int yellow = 0;
            int red = 0;
            List<JobInfo> datas = action.getDatas();
            RedistaskVO vo = new RedistaskVO();
            for (JobInfo jobinfo : datas) {
                //现在循环的是某一个历史任务中，所有关联上的手机号的结果集
                /**
                 * 1、拿到某一个手机号对应的实体pojo---》jobinfo
                 * 2、可以获取到jobinfo中对应的注册信息
                 * 3、进行积分计算，判断预警程度
                 * 4、进行相应的积分进行求和计算
                 * 5、组装vo数据
                 */
                termData.clear();
                termData.add(jobinfo);
                JSONObject jsonObject = generatorResult(termData, conf);
                if (jsonObject == null) {
                    continue;
                }
                if (jsonObject.getString("warnLevel").equals("蓝色预警")) {
                    blue++;
                } else if (jsonObject.getString("warnLevel").equals("橙色预警")) {
                    yellow++;
                } else if (jsonObject.getString("warnLevel").equals("红色预警")) {
                    red++;
                } else {

                }

            }
            vo.setBlueCount(blue);
            vo.setRedCount(red);
            vo.setYellowCount(yellow);
            vo.setCreationTime(DateUtils.getFormatString(action.getCreationTime()));
            vo.setTaskname(action.getTaskname());
            vo.setId(action.getId());
            result.add(vo);
        });
        return (PageImpl<RedistaskVO>) PageUtil.pageBeagin(result.size(), pageNum, pageSize, result);
    }

    @Override
    public JSONObject reportById(String id, String username) {
        Redistask byId = mongoTemplate.findById(id, Redistask.class);
        List<JobInfo> datas = byId.getDatas();
        CreditsWarn conf = creditsService.findCreditsWarnConf(username);
        List<JSONObject> jsonObjects = generatorList(datas, username);
        JSONObject result = getWarnKindObject(conf, jsonObjects);
        result.put("creationTime", DateUtils.getFormatString(datas.get(0).getCreationTime()));
        getTotalKindCount(datas, conf, result);
        return result;
    }

    @Override
    public JSONObject reportByMobileBatch(String[] ids, String username, Boolean ifSelectAll) {
        List<JobInfo> preDatas;
        if(Boolean.TRUE.equals(ifSelectAll)){
            Query query = new Query();
            Criteria criteria = Criteria.where("username").is(username).and("data").exists(true);
            query.addCriteria(criteria);
            preDatas = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        }else{
            Iterable<JobInfo> allById = jobInfoRepository.findAllById(Arrays.asList(ids));
            preDatas = Lists.newArrayList(allById);
        }
        CreditsWarn conf = creditsService.findCreditsWarnConf(username);
        int gamble = 0;
        int loans = 0;
        int yellow = 0;
        int living = 0;
        int game = 0;
        int totalSorce = 0;
        for (JobInfo jobInfo : preDatas) {
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
        }
        JSONObject result = new JSONObject();
        result.put("gamble", gamble);
        result.put("loans", loans);
        result.put("yellow", yellow);
        result.put("living", living);
        result.put("game", game);
        result.put("totalSorce", totalSorce);
        //待定
        result.put("data", preDatas);
//        result.put("warnLevel", getWarnLevel(totalSorce, conf));
//        result.put("creationTime", DateUtils.getFormatString(jobInfo.getCreationTime()));
        return result;
    }

    @Override
    public JSONObject reportByTaskBatch(String[] taskid, String username, Boolean ifSelectAll) {
        JSONObject generator;
        CreditsWarn conf = creditsService.findCreditsWarnConf(username);
        if (Boolean.FALSE.equals(ifSelectAll)) {
            generator = generator(taskid, conf, username);
        } else {
            List<JobinfoVO> jobinfoVOS = apiService.listJob(username);
            List<String> collect = jobinfoVOS.stream().map(JobinfoVO::getTaskId).collect(Collectors.toList());
            String[] strings = collect.toArray(new String[]{});
            generator = generator(strings, conf, username);
        }

        return generator;
    }

    @Override
    public JSONObject reportByPlat(String[] taskid, String webtype, String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("taskid").in(taskid)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        List<JobInfo> list = mongoTemplate.find(query, JobInfo.class, Constans.T_MOBILEDATAS);
        JSONObject result = new JSONObject();
        result.put("data", new HashMap<String, Set<JobInfo>>());

//        Set<String> checkSet = new HashSet<>();

        for (JobInfo jobInfo : list) {
//            checkSet.clear();
            JSONArray data = jobInfo.getData();
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = new JSONObject((Map<String, Object>) data.get(i));

                if (jsonObject.getString("webtype").equals(webtype)) {
//                        if (checkSet.add(aSplit)) {
                    HashMap<String, Set<JobInfo>> hashMap = result.getObject("data", HashMap.class);
                    Set<JobInfo> webname = hashMap.get(jsonObject.getString("webname"));
                    if (webname == null) {
                        Set<JobInfo> set = new HashSet<>();
                        set.add(jobInfo);
                        hashMap.put(jsonObject.getString("webname"), set);
                    } else {
                        webname.add(jobInfo);
                    }
//                        }
                }
            }
        }
        System.out.println(result);
        Set<JobInfo> preData = new HashSet<>();
        result.forEach((k, v) ->

        {
            HashMap<String, Set<JobInfo>> hashMap = (HashMap<String, Set<JobInfo>>) v;
            hashMap.forEach((k2, v2) -> preData.addAll(v2));
            System.out.println(hashMap);
        });
        System.out.println(preData);
        CreditsWarn conf = creditsService.findCreditsWarnConf(username);
        List<JSONObject> jsonObjects = generatorList(list, username);
        JSONObject warnKindObject = getWarnKindObject(conf, jsonObjects);
//        getTotalKindCount(jobInfos, conf, result);
        result.putAll(warnKindObject);
        System.out.println(result);
        return result;
    }
}

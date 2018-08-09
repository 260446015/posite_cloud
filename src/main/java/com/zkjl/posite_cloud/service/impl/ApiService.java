package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.MD5Util;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.service.IApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/9 11:20
 **/
@Service
public class ApiService implements IApiService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JobInfoRepository jobInfoRepository;
    @Resource
    private MongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Override
    public JobInfo createJob(JobDTO jobDTO) {
        String hashKey = getTaskId(jobDTO);
        Query query = new Query();
        query.addCriteria(Criteria.where("hash").is(hashKey)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        JobInfo one = mongoTemplate.findOne(query, JobInfo.class, Constans.T_JOBINFO);
        if(one != null) {
            return one;
        }
        String taskId = getTaskId(jobDTO);
        List<JobInfo> preSaveDatas = new ArrayList<>();
        jobDTO.getDatas().forEach(mobile ->{
            JobInfo jobInfo = new JobInfo();
            jobInfo.setTaskid(taskId);
            jobInfo.setUsername(jobDTO.getUsername());
            jobInfo.setCreationTime(Calendar.getInstance().getTime());
            jobInfo.setMobile(mobile);
            preSaveDatas.add(jobInfo);
        });
        jobInfoRepository.saveAll(preSaveDatas);
        jobDTO.setTaskid(taskId);
        createRedisJob(jobDTO);
        return null;
    }

    private String getTaskId(JobDTO jobBean){
       return MD5Util.encrypt(jobBean.getUsername() + jobBean.getStatus() + jobBean.getLevel());
    }

    private void createRedisJob(JobDTO jobDTO){
        String redisId = jobDTO.getUsername()+"_" + jobDTO.getTaskid() + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
        log.info("当前生成的jobinfo信息:"+jobDTO);
        log.info("当前生成的redisId:"+redisId);
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add(redisId,jobDTO.getDatas());
    }
}

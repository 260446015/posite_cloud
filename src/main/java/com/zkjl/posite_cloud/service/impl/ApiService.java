package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.common.util.MD5Util;
import com.zkjl.posite_cloud.dao.JobInfoRepository;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.service.IApiService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JobInfoRepository jobInfoRepository;
    @Resource
    private MongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Override
    public JobDTO createJob(JobDTO jobDTO) {
        String hashKey = getTaskId(jobDTO);
        Query query = new Query();
        query.addCriteria(Criteria.where("taskid").is(hashKey)).with(Sort.by(Sort.Direction.DESC, "creationTime"));
        JobInfo one = mongoTemplate.findOne(query, JobInfo.class, Constans.T_JOBINFO);
        if (one != null) {
            BeanUtils.copyProperties(one, jobDTO);
            return jobDTO;
        }
        String taskId = getTaskId(jobDTO);
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
        return MD5Util.encrypt(jobBean.getUsername()  + StringUtils.join(jobBean.getDatas(), ","));
    }

    private void createRedisJob(JobDTO jobDTO) {
        String redisId = jobDTO.getUsername() + "_" + jobDTO.getTaskid() + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
        log.info("当前生成的jobinfo信息:" + jobDTO);
        log.info("当前生成的redisId:" + redisId);
        ListOperations listOperations = stringRedisTemplate.opsForList();
        jobDTO.getDatas().forEach(mobile -> listOperations.rightPush(redisId,mobile));
    }

    @Override
    public Boolean updateJob(JobDTO jobDTO) {
        Boolean flag = false;
        try {
            String taskId = getTaskId(jobDTO);
            String status;
            if("start".equalsIgnoreCase(jobDTO.getStatus())){
                status = "stop";
            }else{
                status = "start";
            }
            Integer level;
            level = getLevel(jobDTO);
            jobDTO.setLevel(level);
            String _redisId = jobDTO.getUsername() + "_" + taskId + "_" + jobDTO.getLevel() + "_" + status;
            String redisId = jobDTO.getUsername() + "_" + taskId + "_" + jobDTO.getLevel() + "_" + jobDTO.getStatus();
            redisTemplate.rename(_redisId,redisId);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flag;
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

}

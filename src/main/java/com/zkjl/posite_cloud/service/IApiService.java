package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.exception.CustomerException;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/9 11:17
 **/
public interface IApiService {

    JobDTO createJob(JobDTO jobDTO) throws CustomerException;

    Boolean updateJob(JobDTO jobDTO);

    JSONObject realTimeData(String username) throws Exception;

    JSONObject developmentData(String username) throws Exception;

    List<JSONObject> realTimeRegist(String username);

    JSONObject getSentiment(SentimentDTO sentimentDTO, String userSentiment);

    JSONObject listJob(String username);

    JSONObject searchByTaskid(String taskId, Integer pageNum, Integer pageSize, String msg);

    JSONObject searchByTaskidPlan(String taskId);

    boolean deleteBatch(String[] ids, String username);

    JSONObject listAllJob(String username);

    boolean taskAssignment(String[] taskid, String[] userid);

    boolean updatePersonMark(Integer handleMark, String id) throws CustomerException;
}

package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.dto.SentimentDTO;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/9 11:17
 **/
public interface IApiService {

    JobDTO createJob(JobDTO jobDTO);

    Boolean updateJob(JobDTO jobDTO);

    JSONObject realTimeData(String username) throws Exception;

    JSONObject developmentData(String username) throws Exception;

    List<JSONObject> realTimeRegist(String username);

    JSONObject getSentiment(SentimentDTO sentimentDTO);

    List<JobinfoVO> listJob(String username);

    JSONObject searchByTaskid(String taskId);
}

package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import org.springframework.data.domain.PageImpl;

/**
 * @author yindawei
 * @date 2018/8/9 11:17
 **/
public interface IApiService {

    JobDTO createJob(JobDTO jobDTO);

    Boolean updateJob(JobDTO jobDTO);

    JSONObject realTimeData(String username) throws Exception;

    JSONObject developmentData(String username) throws Exception;

    PageImpl<JobInfo> realTimeRegist(String username, Integer pageNum, Integer pageSize);
}

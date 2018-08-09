package com.zkjl.posite_cloud.service;

import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;

/**
 * @author yindawei
 * @date 2018/8/9 11:17
 **/
public interface IApiService {

    JobInfo createJob(JobDTO jobDTO);
}

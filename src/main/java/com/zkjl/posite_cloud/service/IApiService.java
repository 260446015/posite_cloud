package com.zkjl.posite_cloud.service;

import com.zkjl.posite_cloud.domain.dto.JobDTO;

/**
 * @author yindawei
 * @date 2018/8/9 11:17
 **/
public interface IApiService {

    JobDTO createJob(JobDTO jobDTO);

    Boolean updateJob(JobDTO jobDTO);
}

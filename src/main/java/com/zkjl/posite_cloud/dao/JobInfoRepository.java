package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.vo.JobinfoVO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/9 11:46
 **/
public interface JobInfoRepository extends MongoRepository<JobInfo,String> {
    List<JobInfo> findByUsername(String username);

    List<JobinfoVO> findByTaskid(String taskId);
}

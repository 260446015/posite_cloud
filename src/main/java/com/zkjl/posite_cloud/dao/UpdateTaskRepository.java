package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.UpdateTask;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/24 17:55
 **/
public interface UpdateTaskRepository extends MongoRepository<UpdateTask,String> {
    List<UpdateTask> findByTaskid(String taskid);
}

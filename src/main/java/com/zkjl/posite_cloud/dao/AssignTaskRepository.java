package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.AssignTask;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author yindawei
 * @date 2018/9/30 10:54
 **/
public interface AssignTaskRepository extends MongoRepository<AssignTask, String> {
}

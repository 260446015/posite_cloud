package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.Log;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/16 16:12
 **/
public interface LogRepository extends MongoRepository<Log, String> {
    List<Log> findByUsername(String username);
}

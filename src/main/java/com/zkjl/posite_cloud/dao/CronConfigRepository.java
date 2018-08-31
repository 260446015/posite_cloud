package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.CronConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author yindawei
 * @date 2018/8/31 10:25
 **/
public interface CronConfigRepository extends MongoRepository<CronConfig,String> {
}

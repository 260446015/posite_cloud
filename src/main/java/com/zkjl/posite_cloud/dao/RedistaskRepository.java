package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.Redistask;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/16 18:50
 **/
public interface RedistaskRepository extends MongoRepository<Redistask, String> {
    List<Redistask> findByUsername(String username);

    void deleteByTaskid(String id);
}

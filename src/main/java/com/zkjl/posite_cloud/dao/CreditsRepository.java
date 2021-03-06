package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/14 18:03
 **/
public interface CreditsRepository extends MongoRepository<CreditsWarn, String> {
    List<CreditsWarn> findByUsername(String username);
}

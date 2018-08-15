package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author yindawei
 * @date 2018/8/15 17:35
 **/
public interface RoleRepository extends MongoRepository<Role, String> {
    Role findByRolename(String jobLevel);
}

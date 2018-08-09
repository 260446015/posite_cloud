package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {
    User findByUsernameAndPassword(String username, String password);
}

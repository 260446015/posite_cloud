package com.zkjl.posite_cloud.dao;

import com.zkjl.posite_cloud.domain.pojo.Notice;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author yindawei
 * @date 2018/10/10 16:41
 **/
public interface NoticeRepository extends MongoRepository<Notice, String> {
}

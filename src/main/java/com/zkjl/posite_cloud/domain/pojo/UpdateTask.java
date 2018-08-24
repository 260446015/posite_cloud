package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author yindawei
 * @date 2018/8/24 17:47
 **/
@Data
@Document(collection = "update_task")
public class UpdateTask {

    @Id
    private String id;
    private String taskid;
    private String username;
    private String redisTaskid;
    private Boolean ifFinish;
    private Date creationTime;
    private String taskname;
    private Date updateTime;
}

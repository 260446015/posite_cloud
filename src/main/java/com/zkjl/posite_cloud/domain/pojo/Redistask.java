package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author yindawei
 * @date 2018/8/16 18:47
 **/
@Document(collection = "redistask")
@Data
public class Redistask implements Serializable {

    private static final long serialVersionUID = -2493293907861519932L;

    @Id
    private String taskid;
    private String username;
    private String redisTaskid;
    private Boolean ifFinish;
    private Date creationTime;
    private String taskname;

    public Redistask(String username, String taskid, Boolean ifFinish, String taskname) {
        this.username = username;
        this.taskid = taskid;
        this.ifFinish = ifFinish;
        this.creationTime = Calendar.getInstance().getTime();
        this.taskname = taskname;
    }

    public Redistask() {
    }
}

package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/16 18:47
 **/
@Document(collection = "redistask")
@Data
public class Redistask implements Serializable {

    private static final long serialVersionUID = -2493293907861519932L;

    @Id
    private String id;
    private String taskid;
    private String username;
    private String redisTaskid;
    private Boolean ifFinish;
    private Date creationTime;
    private String taskname;
    private Integer _version;
    private List<JobInfo> datas;

    public Redistask(String username, String taskid, Boolean ifFinish, String taskname) {
        this.username = username;
        this.taskid = taskid;
        this.ifFinish = ifFinish;
        this.creationTime = Calendar.getInstance().getTime();
        this.taskname = taskname;
        this._version = 0;
    }

    public Redistask() {
    }
}

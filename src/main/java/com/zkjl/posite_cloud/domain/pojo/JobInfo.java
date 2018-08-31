package com.zkjl.posite_cloud.domain.pojo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yindawei
 * @date 2018/8/9 11:18
 **/
@Data
@Document(collection = "mobiledatas")
public class JobInfo implements Serializable {
    private static final long serialVersionUID = 3964913418638634216L;

    @Id
    private String id;
    private String taskid;
    private String username;
    private String mobile;
    private String mobileUser;
    private JSONArray data;
    private Date creationTime;
    private Date updateTime;
    private Boolean ifSendEmail;
}

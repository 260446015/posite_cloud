package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yindawei
 * @date 2018/9/30 10:25
 **/
@Document(collection = "assign_task")
@Data
public class AssignTask implements Serializable {

    private static final long serialVersionUID = 2686852042795978415L;
    @Id
    private String id;
    private String userid;
    private String taskid;
    private Date createTime;

}

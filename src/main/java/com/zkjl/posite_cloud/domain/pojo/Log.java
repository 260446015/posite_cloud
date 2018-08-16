package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/16 16:09
 **/
@Document(collection = "log")
@Data
public class Log implements Serializable {
    private static final long serialVersionUID = -6107864687161120242L;
    @Id
    private String id;
    private String username;
    private String content;
    private String cretionTime;
}

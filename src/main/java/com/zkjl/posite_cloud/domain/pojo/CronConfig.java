package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/31 10:00
 **/
@Data
@Document(collection = "cron_config")
public class CronConfig implements Serializable {
    private static final long serialVersionUID = -3380205144908484158L;
    @Id
    private String id;
    private String cron;
    private String username;
    private String creationTiime;
}

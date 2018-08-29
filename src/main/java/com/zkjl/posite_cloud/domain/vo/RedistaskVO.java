package com.zkjl.posite_cloud.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/29 17:18
 **/
@Data
public class RedistaskVO implements Serializable {
    private static final long serialVersionUID = 4661853471355524078L;

    private String id;
    private String taskname;
    private Integer redCount;
    private Integer yellowCount;
    private Integer blueCount;
    private String creationTime;

}

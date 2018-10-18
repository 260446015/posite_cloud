package com.zkjl.posite_cloud.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/15 10:31
 **/
@Data
public class JobinfoVO implements Serializable {
    private static final long serialVersionUID = -2524390291306720047L;
    private String taskId;
    private String creationTime;
    private Boolean ifFinish;
    private String taskname;
    private Boolean reportStatus;
    private Integer uploadSize;
}

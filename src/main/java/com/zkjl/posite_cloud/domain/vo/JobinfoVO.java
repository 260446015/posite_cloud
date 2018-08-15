package com.zkjl.posite_cloud.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yindawei
 * @date 2018/8/15 10:31
 **/
@Data
public class JobinfoVO implements Serializable {
    private static final long serialVersionUID = -2524390291306720047L;
    private String taskId;
    private Date creationTime;
    private Boolean ifFinish;
}
